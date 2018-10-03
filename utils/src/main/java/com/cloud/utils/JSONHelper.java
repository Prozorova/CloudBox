package com.cloud.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONHelper {
	
	private static JSONHelper jsonHelper = new JSONHelper();
	private static ObjectMapper   mapper = new ObjectMapper();
	
	private JSONHelper() {}
	
	public static byte[] writeJson(JSON jsonMsg) throws JsonProcessingException {
		return mapper.writeValueAsBytes(jsonMsg);
	}
	
	// только для целей тестирования
	public static void readToFile(Object msg) throws FileNotFoundException, IOException {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(new FileOutputStream("111.json"), msg);
	}
}
