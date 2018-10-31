package com.cloud.utils.exep;

public class CheckSumException extends Exception{

	public CheckSumException(String fileName) {
		super("The received file ("+fileName+") has an incorrect checksum");
	}
}
