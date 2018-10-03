package com.cloud.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.cloud.utils.jsonQueries.StandardJsonQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.netty.buffer.ByteBuf;

public class JSONHelper {
	
	private static JSONHelper jsonHelper = new JSONHelper();
	private static ObjectMapper   mapper = new ObjectMapper();
	
	private JSONHelper() {}
	
	public static byte[] writeJson(StandardJsonQuery jsonMsg) throws JsonProcessingException {
		return mapper.writeValueAsBytes(jsonMsg);
	}
	
	// только для целей тестирования
	public static void readToFile(Object msg) throws FileNotFoundException, IOException {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Charset charset = Charset.defaultCharset();
		msg = mapper.readValue(((ByteBuf) msg).toString(charset), StandardJsonQuery.class);
		mapper.writeValue(new FileOutputStream("111.json", false), msg);
	}
}