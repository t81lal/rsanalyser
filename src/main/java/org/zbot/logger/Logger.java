package org.zbot.logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Bibl (don't ban me pls) <br>
 * @created 9 Mar 2015 at 22:22:59 <br>
 */
public class Logger {

	private static final Logger DEFAULT_LOGGER = new Logger();

	private final Dispatcher dispatcher;

	public Logger() {
		dispatcher = new Dispatcher();
	}

	public void addListener(LoggerListener l) {
		dispatcher.addListener(l);
	}

	public void removeListener(LoggerListener l) {
		dispatcher.removeListener(l);
	}

	public void clear() {
		dispatcher.clear();
	}

	public void secret(Object msg) {
		dispatch(LEVEL.SECRET, msg);
	}

	public void trace(Object msg) {
		dispatch(LEVEL.TRACE, msg);
	}

	public void debug(Object msg) {
		dispatch(LEVEL.DEBUG, msg);
	}

	public void info(Object msg) {
		dispatch(LEVEL.INFO, msg);
	}

	public void warn(Object msg) {
		dispatch(LEVEL.WARN, msg);
	}

	public void fatal(Object msg) {
		dispatch(LEVEL.FATAL, msg);
	}

	private void dispatch(LEVEL level, Object msg) {
		if (msg == null)
			return;
		Message<?> m = null;
		if (msg instanceof Throwable) {
			m = new ExceptionMessage(level, (Throwable) msg, time());
		} else {
			m = new TextMessage(level, msg.toString(), time());
		}
		dispatcher.dispatch(m);
	}

	private String time() {
		return new Date().toString().split(" ")[3];
	}

	public static Logger getDefaultLogger() {
		return DEFAULT_LOGGER;
	}

	private static class Dispatcher implements Runnable {

		private static final ExecutorService SERVICE = Executors.newFixedThreadPool(1);

		private final List<LoggerListener> listeners;
		private final List<Message<?>> readyText;

		private boolean clear;
		private boolean queue;

		private Dispatcher() {
			listeners = new ArrayList<LoggerListener>();
			readyText = new ArrayList<Message<?>>();
			clear = false;
			queue = true;
		}

		synchronized void addListener(LoggerListener l) {
			listeners.add(l);
		}

		synchronized void removeListener(LoggerListener l) {
			listeners.remove(l);
		}

		synchronized void dispatch(Message<?> msg) {
			readyText.add(msg);
			if (queue) {
				queue = false;
				SERVICE.execute(this);
			}
		}

		synchronized void clear() {
			clear = true;
			readyText.clear();
			if (queue) {
				queue = false;
				SERVICE.execute(this);
			}
		}

		@Override
		public synchronized void run() {
			if (clear) {
				for (LoggerListener l : listeners) {
					l.clear();
				}
			}
			for (Message<?> msg : readyText) {
				for (LoggerListener l : listeners) {
					l.update(msg);
				}
			}
			readyText.clear();
			clear = false;
			queue = true;
		}
	}

	public static abstract class Message<T> {
		private final LEVEL level;
		private final String timeStamp;
		private final String header;

		public Message(LEVEL level, String timeStamp) {
			this.level = level;
			this.timeStamp = timeStamp;

			header = header();
		}

		public LEVEL getLevel() {
			return level;
		}

		public String getTimeStamp() {
			return timeStamp;
		}

		public String getHeader() {
			return header;
		}

		public abstract T getMsg();

		protected abstract String header();

		@Override
		public String toString() {
			return String.format("%s%s", header, getMsg().toString());
		}
	}

	public static class TextMessage extends Message<String> {
		private final String msg;

		public TextMessage(LEVEL level, String msg, String timeStamp) {
			super(level, timeStamp);
			this.msg = msg;
		}

		@Override
		public String getMsg() {
			return msg;
		}

		@Override
		protected String header() {
			return String.format("[%s] [%s]%s: ", getTimeStamp(), getLevel().name(), getLevel().format());
		}
	}

	public static class ExceptionMessage extends Message<Throwable> {
		private final Throwable msg;

		public ExceptionMessage(LEVEL level, Throwable msg, String timeStamp) {
			super(level, timeStamp);
			this.msg = msg;
		}

		@Override
		public Throwable getMsg() {
			return msg;
		}

		@Override
		protected String header() {
			return String.format("[%s] [%s]%s: ", getTimeStamp(), getLevel().name(), getLevel().format());
		}
	}

	public static enum LEVEL {
		SECRET(), TRACE(" "), DEBUG(" "), INFO("  "), WARN("  "), FATAL(" ");

		private final String format;

		private LEVEL() {
			this("");
		}

		private LEVEL(String format) {
			this.format = format;
		}

		public String format() {
			return format;
		}
	}
}