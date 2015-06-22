package org.nullbool.byeallatori;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SSLTrustManager implements X509TrustManager {

	private X509TrustManager trustManager;
	private List<Certificate> certs = new ArrayList<Certificate>();
	private final String keyfile;

	// unused
	public void addCert(Certificate cert, boolean importcert) {
		try {
			if(importcert) {
				Runtime.getRuntime().exec("keytool -importcert ...");
			} else {
				this.certs.add(cert);
			}

			this.loadKey();
		} catch (Exception var3) {
			var3.printStackTrace();
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] var1, String var2) throws CertificateException {
		this.trustManager.checkClientTrusted(var1, var2);
	}

	public static void init() {
		System.out.println("init " + "SSL Manager");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] var1, String var2) throws CertificateException {
		try {
			this.trustManager.checkServerTrusted(var1, var2);
		} catch (CertificateException var3) {
			System.out.println("Found an UNKNOWN host! Please report on the forums!");
		}
	}

	public SSLTrustManager(String file) throws Exception {
		this.keyfile = file;
		this.loadKey();
	}

	public void loadKey() throws Exception {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		FileInputStream fis = new FileInputStream(this.keyfile);

		try {
			keyStore.load(fis, "secret0044".toCharArray());
		} catch (Throwable e) {
			fis.close();
			throw e;
		}

		fis.close();
		Iterator<Certificate> it = this.certs.iterator();

		while(it.hasNext()) {
			Certificate cert = it.next();
			keyStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
		}

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

		for(int i = 0; i < trustManagers.length; i++) {
			if(trustManagers[i] instanceof X509TrustManager) {
				this.trustManager = (X509TrustManager)trustManagers[i];
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.trustManager.getAcceptedIssuers();
	}
}