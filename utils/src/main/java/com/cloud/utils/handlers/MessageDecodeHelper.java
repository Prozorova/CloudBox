package com.cloud.utils.handlers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;

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
public class MessageDecodeHelper  {

	// для работы с json
	private static ObjectMapper mapper = new ObjectMapper();

	private static Charset charset = Charset.defaultCharset();
	
	/**
	 * Распарсить json
	 * @param in байты, полученные из сети
	 * @return объект StandardJsonQuery (json со служебной информацией)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static StandardJsonQuery decode(ByteBuf in) 
			throws JsonParseException, JsonMappingException, IOException {
		
		int length = in.readInt();
		
		StandardJsonQuery jsonQuery = mapper.readValue(in.readBytes(length).toString(charset),
                                                       StandardJsonQuery.class);
			
		// TODO для тестирования - убрать
		mapper.writeValue(new FileOutputStream("111.json", false), jsonQuery);

		return jsonQuery;

	}

	/**
	 * Загрузить файл, полученный из сети
	 * @param in байты, полученные из сети
	 * @param length
	 * @param pos
	 * @param file файл для записи
	 * @throws IOException
	 */
	public static void recieveFile(ByteBuf in, int length, long pos, Path filePath) throws IOException {
		
		try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
			System.out.println("GETTING FILE: " + filePath + ", write from pos " + pos);
			raf.seek(pos);

			byte[] data = new byte[length];
			in.readBytes(data);

			raf.write(data);
		}
		
	}
}
