package com.tdquery.exception;

public class OutputProcessingException extends Exception {

	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 1901559642334359921L;

	public OutputProcessingException(String message) {
		super(message);
	}

	public OutputProcessingException(String message, Throwable e) {
		super(message, e);
	}
}
