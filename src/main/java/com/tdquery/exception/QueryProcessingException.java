package com.tdquery.exception;

public class QueryProcessingException extends Exception {

	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 1901559642334359921L;

	public QueryProcessingException(String message) {
		super(message);
	}

	public QueryProcessingException(String message, Throwable e) {
		super(message, e);
	}
}
