package com.tdquery;

public class QueryException extends Exception {

	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 1901559642334359921L;

	public QueryException(String message) {
		super(message);
	}

	public QueryException(String message, Throwable e) {
		super(message, e);
	}
}
