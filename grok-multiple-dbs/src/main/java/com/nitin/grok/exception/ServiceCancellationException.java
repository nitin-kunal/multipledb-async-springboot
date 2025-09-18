package com.nitin.grok.exception;

public class ServiceCancellationException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceCancellationException(String msg, Throwable cause) { super(msg, cause); }
}