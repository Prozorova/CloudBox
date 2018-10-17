package com.cloud.utils.exep;

import java.io.IOException;

public class IncorrectPathException extends IOException{

	public IncorrectPathException(String message) {
		super(message + "expected path to directory.");
	}
}
