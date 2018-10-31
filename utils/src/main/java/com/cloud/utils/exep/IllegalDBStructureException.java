package com.cloud.utils.exep;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class IllegalDBStructureException extends SQLException {
	
	public IllegalDBStructureException(String message) {
		super("Illegal DB Structure: " + message);
	}

}
