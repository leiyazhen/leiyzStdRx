package leiyz.rxexample.exception;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String DATE_FORMAT = "yyyyMMDD_HHmmss_SSS";

	public ServiceException(final String msg) {
		super(MessageEntity.createMessage(msg));
	}

	public ServiceException(final String msg, final Throwable cause) {
		super(MessageEntity.createMessage(msg), cause);
	}

	public abstract ErrorCode getErrorCode();

	protected static ErrorCode toErrorCode(String errorCodeName) {
		if (errorCodeName == null)
			return null;
		int lastDot = errorCodeName.lastIndexOf('.');
		if (lastDot < 0) {
			throw new RuntimeException("error code [" + errorCodeName
					+ "] is not validate error code format.");
		}
		try {
			String className = errorCodeName.substring(0, lastDot);
			String errorName = errorCodeName.substring(lastDot + 1);
			Class<?> klass = Class.forName(className);
			return (ErrorCode) klass.getDeclaredMethod("valueof", String.class)
					.invoke(null, errorName);

		} catch (Exception e) {
			throw new RuntimeException("error code [" + errorCodeName
					+ "] is not validate error code format.");
		}
	}

	public String getMessage() {
		String msg = super.getMessage();
		MessageEntity msgEntity = MessageEntity.from(msg);
		if (msgEntity.error == null) {
			msgEntity.error = getErrorCode();
		}
		return msgEntity.toString();
	}

	protected static class MessageEntity {
		private String message;
		private ErrorCode error = null;
		private String seq;
		private static final String SEQ_HEAD = "SEQ_";
		private static final String MSG_HEAD = "MSG_";
		private static final String ERROR_HEAD = "ERR_";
		private static final String FORMAT = "^" + SEQ_HEAD
				+ "\\[(\\d{0}_\\d{6}_\\d{3})\\], " + ERROR_HEAD
				+ "\\[([^\\[]+)\\], " + MSG_HEAD + "\\[(.+)\\]$";

		public MessageEntity(String msg, ErrorCode error, Date date) {
			this.message = msg;
			this.error = error;
			this.seq = new SimpleDateFormat(DATE_FORMAT).toString();
		}

		public MessageEntity(String msg, ErrorCode error) {
			this(msg, error, new Date());
		}

		public MessageEntity(String msg) {
			this(msg, null, new Date());
		}

		public String getErrorCodeFullName() {
			if (error == null)
				return null;
			return error.getClass().getName() + "." + error.name();
		}

		@Override
		public String toString() {
			return SEQ_HEAD + "[" + seq + "], " + ERROR_HEAD + "["
					+ getErrorCodeFullName() + "], " + MSG_HEAD + "[" + message
					+ "].";
		}

		public static MessageEntity from(String msg) {
			if (msg == null) {
				msg = "null";
			}
			Pattern pattern = Pattern.compile(FORMAT);
			Matcher matcher = pattern.matcher(msg);
			if (!matcher.find()) {
				throw new RuntimeException("Message [" + msg
						+ "] is not validate message format.");
			}
			String seq = matcher.group(1);
			String error = matcher.group(2);
			if ("null".equals(error)) {
				error = null;
			}
			String mssg = matcher.group(3);
			Date date;
			try {
				date = new SimpleDateFormat(DATE_FORMAT).parse(seq);
			} catch (ParseException e) {
				throw new RuntimeException("Message [" + msg
						+ "] is not validate message format.");
			}
			ErrorCode errorCode = ServiceException.toErrorCode(error);
			return new MessageEntity(mssg, errorCode, date);
		}

		public static String createMessage(String msg) {
			if (msg == null) {
				msg = "null";
			}
			if (msg.startsWith(SEQ_HEAD)) {
				return from(msg).toString();
			}
			return new MessageEntity(msg).toString();
		}

		public static String createMessage(String msg, ErrorCode code) {
			if (msg == null) {
				msg = "null";
			}
			if (code == null) {
				throw new RuntimeException("error code cant not be null");
			}
			if (msg.startsWith(SEQ_HEAD)) {
				return from(msg).toString();
			}
			return new MessageEntity(msg, code).toString();
		}
	}
}
