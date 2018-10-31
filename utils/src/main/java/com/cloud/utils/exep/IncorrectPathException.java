package com.cloud.utils.exep;


@SuppressWarnings("serial")
public class IncorrectPathException extends Exception{

	public IncorrectPathException(String message) {
		super(message + ": expected path to directory.");
	}
	
	public IncorrectPathException(String message, Exception e) {
		super("Incorrect path: "+message+ ", "+e.getMessage(), e);
	}
}
