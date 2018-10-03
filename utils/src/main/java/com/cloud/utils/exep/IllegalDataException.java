package com.cloud.utils.exep;

public class IllegalDataException extends RuntimeException {
	
	public IllegalDataException(Class<?> clazz) {
		super("Illegal file type for transmitting: " + clazz);
	}
}
