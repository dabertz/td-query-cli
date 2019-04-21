package com.tdquery.exception;

public class CommandException extends Exception {

	/**
	 * Auto generated
	 */
	private static final long serialVersionUID = -6768991471225646702L;
	public String fieldName;

	public CommandException(String message) {
		super(message);
	}

	public CommandException(String message, String fieldName) {
		super(message);
		this.fieldName = fieldName;
	}

	public CommandException(String message, Throwable e) {
		super(message, e);
	}
}
