package com.tdquery.exception;

public class ParseOptionValueException extends CommandException{
	
	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 8222916197276787457L;

	public ParseOptionValueException(String message, String fiedname) {
		super(message, fiedname);
	}
}
