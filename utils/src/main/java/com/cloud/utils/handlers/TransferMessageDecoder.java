package com.cloud.utils.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;

/**
 * Используется в первом хендлере при получении данных из сети:
 * парсит json, загружает файл
 * @author prozorova 10.10.2018
 */
public class TransferMessageDecoder  {

	// для работы с json
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Распарсить json
	 * @param in байты, полученные из сети
	 * @return объект StandardJsonQuery (json со служебной информацией)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static StandardJsonQuery decode(ByteBuf in) throws JsonParseException, JsonMappingException, IOException {

		// читаем json
		int jsonLength = in.readMedium();

		Charset charset = Charset.defaultCharset();
		StandardJsonQuery jsonQuery = mapper.readValue(in.readBytes(jsonLength).toString(charset),
				                                       StandardJsonQuery.class);
		// TODO для тестирования - убрать
		mapper.writeValue(new FileOutputStream("111.json", false), jsonQuery);
		
		return jsonQuery;
	}
	
	/**
	 * Загрузить файл, полученный из сети
	 * @param in байты, полученные из сети
	 * @param length длина файла (кол-во байт)
	 * @param filePath путь к файлу для записи
	 * @return объект File, указывающий на загруженный файл
	 * @throws IOException
	 */
	public static File recieveFile(ByteBuf in, int length, String filePath) throws IOException {
		
		Path path = Paths.get(filePath);
		try {
		in.readBytes(Files.newOutputStream(path, 
				                           StandardOpenOption.CREATE), 
				     length);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return path.toFile();
	}
}
