package com.cloud.utils.exep;

import com.cloud.utils.queries.StandardJsonQuery;

/**
 * Кидается при попытке передачи некорректных данных
 * @author prozorova 10.10.2018
 */
public class IllegalDataException extends Exception {
	
	public IllegalDataException(Class<?> clazz) {
		super("Illegal file type for transmitting: " + clazz);
	}
	
	public IllegalDataException(StandardJsonQuery.QueryType jsonType) {
		super("Illegal type of json query: " + jsonType);
	}
	
	public IllegalDataException(String msg) {
		super("Illegal data for this operation: " + msg);
	}
}
