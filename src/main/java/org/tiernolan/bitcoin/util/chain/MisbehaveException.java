package org.tiernolan.bitcoin.util.chain;

public class MisbehaveException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public static final int MINOR = 1;
	public static final int WARN = 20;
	public static final int CRITICAL = 100;
	
	private final int severity;

	public MisbehaveException(int severity, String message) {
		super(message);
		this.severity = severity;
	}
	
	public MisbehaveException(int severity, String message, Throwable cause) {
		super(message, cause);
		this.severity = severity;
	}
	
	public int getSeverity() {
		return severity;
	}
	
}
