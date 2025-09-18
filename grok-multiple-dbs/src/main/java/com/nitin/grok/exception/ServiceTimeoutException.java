package com.nitin.grok.exception;

public class ServiceTimeoutException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceTimeoutException(String msg, Throwable cause) { super(msg, cause); }
}