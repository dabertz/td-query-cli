package com.tdquery.exception;

public class RequiredArgumentException extends CommandException{
	
	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 8222916197276787457L;

	public RequiredArgumentException(String message, String fiedname) {
		super(message, fiedname);
	}
}
