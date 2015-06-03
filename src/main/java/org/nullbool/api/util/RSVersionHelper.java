package org.nullbool.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Bibl (don't ban me pls)
 * @created 3 Jun 2015 21:27:05
 */
public class RSVersionHelper {

	public static String getServerAddress(int world) {
		return "oldschool" + world + ".runescape.com";
	}

	public static int getVersion(String address, int minor, int major) throws IOException {
		class DaemonFactory implements ThreadFactory {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		}

		ExecutorService service = Executors.newFixedThreadPool(4, new DaemonFactory());
		AtomicInteger i = new AtomicInteger();
		List<ResponseChecker> runs = new ArrayList<ResponseChecker>();
		while (true) {
			if (i.get() == 4) {
				for (ResponseChecker r : runs) {
					if (r.worked) {
						service.shutdownNow();
						return r.version;
					}
				}
			} else {
				ListIterator<ResponseChecker> it = runs.listIterator();
				while (it.hasNext()) {
					ResponseChecker r = it.next();
					if (r.done) {
						it.remove();
					}
				}
				for (int j = i.get(); j < 4; j++) {
					ResponseChecker run = new ResponseChecker(i, address, minor++);
					runs.add(run);
					service.execute(run);
				}
			}
		}
	}

	private static byte[] getHandshake(int version) {
		ByteBuffer nigga = ByteBuffer.allocate(4 + 1); // handshake type + version
		nigga.put((byte) 15);
		nigga.putInt(version);
		return nigga.array();
	}

	private static final class BadResponseException extends IOException {
		private static final long serialVersionUID = -3684012747255222205L;

		private BadResponseException(int response, int expected) {
			super(String.format("Expected response %d, got %d!", expected, response));
		}
	}

	private static class ResponseChecker implements Runnable {

		private final AtomicInteger i;
		private final String address;
		private final int version;
		private boolean worked = false;
		private boolean done = false;

		public ResponseChecker(AtomicInteger i, String address, int version) {
			this.i = i;
			this.address = address;
			this.version = version;
		}

		@Override
		public void run() {
			try {
				i.incrementAndGet();
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(address, 43594), 8000);
				InputStream input = socket.getInputStream();
				OutputStream output = socket.getOutputStream();
				output.write(getHandshake(version));
				output.flush();
				while (true) {
					if (input.available() <= 0)
						continue;
					int response = input.read();
					if (response == 0) {        // found version!
						worked = true;
					} else if (response == 6) { // bad version
						worked = false;
						socket.close();
						break;
					} else {
						socket.close();
						throw new BadResponseException(version, 1);
					}
				}
			} catch (Exception e) {
			} finally {
				i.decrementAndGet();
				done = true;
			}
		}
	}
}