/**
 * 
 */
package org.nullbool.byeallatori;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

/**
 * @author Bibl (don't ban me pls)
 * @created 21 Jun 2015 20:16:05
 */
public class ConnectionHelper {

	public static HttpsURLConnection openSecureHandle(String url) throws Exception {
		File key_file;
		HttpsURLConnection connection;
		String key_file_name;
		
		mainBlockLabel: {
			connection = (HttpsURLConnection)(new URL(url)).openConnection();
			key_file_name = "xna_uop.str";
			File strFile = new File((new StringBuilder()).insert(0, Constants.DATA_DIR).append(key_file_name).toString());
			if(!strFile.exists()) {
				strFile.createNewFile();
				InputStream in_stream = null;
				FileOutputStream out_stream = null;
				boolean failed = false;

				try {
					failed = true;
					in_stream = ConnectionHelper.class.getClassLoader().getResourceAsStream(key_file_name);
					out_stream = new FileOutputStream(strFile);
					StreamHelper.transfer8k(in_stream, out_stream);
					failed = false;
				} finally {
					if(failed) {
						if(in_stream != null) {
							in_stream.close();
						}

						if(out_stream != null) {
							out_stream.close();
						}

					}
				}

				if(in_stream != null) {
					in_stream.close();
				}

				if(out_stream != null) {
					key_file = strFile;
					out_stream.close();
					break mainBlockLabel;
				}
			}

			key_file = strFile;
		}

		key_file_name = key_file.getPath();
		
		key_file_name = new File("C:/Users/Bibl/Desktop/osbots shit nigga/xna_uop.str").getAbsolutePath();
				
		connection.setSSLSocketFactory(loadSSLContext(key_file_name).getSocketFactory());
		return connection;
	}

	public static SSLContext loadSSLContext(String file) throws Exception {
		SSLTrustManager[] trustmanagers = new SSLTrustManager[]{new SSLTrustManager(file)};
		SSLContext sslcontext = SSLContext.getInstance("SSL");
		sslcontext.init((KeyManager[])null, trustmanagers, (SecureRandom)null);
		return sslcontext;
	}

}