package com.cloud.utils.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.cloud.utils.exep.CheckSumException;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.queries.PathContainer;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.StandardJsonQuery.QueryType;
import com.cloud.utils.queries.json.JsonGetFile;
import com.cloud.utils.queries.json.JsonSendFile;
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

	private static final Logger logger = Logger.getLogger(MessageDecodeHelper.class);
	
	// для работы с json
	private static ObjectMapper mapper = new ObjectMapper();
	private static Charset charset = Charset.defaultCharset();
	
	// ожидаемые файлы
	private final Map<String, JsonSendFile> files = new HashMap<>();
	
	private final ExecutorService service = Executors.newFixedThreadPool(10);
	
	public static final String CLIENT_COM_SENDFILE = "-67489267";
	

	public StandardJsonQuery decodeTransference(ByteBuf frame, String pathClient) throws IOException {
		
		StandardJsonQuery out = null;
		int code = frame.readInt();
		
		// если получили json
		if (code == FileTransferHelper.CODE_JSON) {

			StandardJsonQuery jsonQuery = decodeJson(frame);
			logger.debug("Json query recieved: "+jsonQuery.getQueryType());
			
			if (jsonQuery instanceof PathContainer) {
				
				PathContainer json = (PathContainer)jsonQuery;
				String path = null;
				
				// если в pathClient есть некая кодовая последовательность - это декодирование на клиенте,
				// в этом случае путь менять не надо - он сохранен на клиенте
				if (pathClient != CLIENT_COM_SENDFILE) {
					// вытаскиваем заданный путь к файлу или папке
					path = pathClient +                // путь к папке
					       json.getFilePath();         // путь к запрашиваему файлу или папке
				
					if (json instanceof JsonGetFile) 
						((JsonGetFile)json).setFilePathOld(json.getFilePath());

					json.setFilePath(path);     // меняем путь к файлу
				}
				
				if (jsonQuery.getQueryType() == QueryType.SEND_FILE) {
					JsonSendFile jsonSendFile = (JsonSendFile)jsonQuery;
					
					if (pathClient != CLIENT_COM_SENDFILE)
						jsonSendFile.setFilePath(path+jsonSendFile.getFileName());
					
					files.put(jsonSendFile.getFileCheckSum(), jsonSendFile); // добавляем в список файлов на получение
					
					File file = new File(jsonSendFile.getFilePath());
					Files.deleteIfExists(file.toPath());  // удаляем старый файл, если есть
				} 
			}
			
			// отправляем запрос дальше во всех случаях, кроме получения файла на сервере:
		    // получение файла подтвердим (или нет -  в зависимости от исхода операции) 
		    // после его фактического получения.
			// если получили файл на клиенте - json надо вернуть, чтобы клиентский декодер
			// подставил путь для данного файла
			if (jsonQuery.getQueryType() != QueryType.SEND_FILE || pathClient == CLIENT_COM_SENDFILE)
				out = jsonQuery;
			
		// если получили файл или его часть
		} else if (code == FileTransferHelper.CODE_FILE) {
			
			int length = frame.readInt();
			long position = frame.readLong();
			
			// идентификация файла - определяем, что за кусок нам пришел, к чему он относится
			String fileID = frame.readCharSequence(32, Charset.defaultCharset()).toString();
			
			logger.debug("RECIEVING FILE: " + fileID  + " *** " + length);
			JsonSendFile json = files.get(fileID);
			
			// если в списке на получение этого файла нет - произошла ошибка при передаче,
			// принимать следующий кусок уже нет смысла
			if (json !=  null) {
				
				// создаем новый файл, если он еще не создан
				File file = new File(json.getFilePath());
				if (!file.exists())
					Files.createFile(file.toPath());


				try {     // получение файла (или его части)
					recieveFile(frame, length, position, file.toPath());
				} catch (Throwable e) {
					logger.error("Recieving file or file part failed: "+e.getMessage(), e);

					// произошла ошибка при передаче - меняем параметр
					json.setPartsAmount(-1);
				}

				// уменьшаем количество частей, которые осталось получить
				json.setPartsAmount(json.getPartsAmount()-1);

				// проверяем, получили или нет файл целиком
				if (json.getPartsAmount() == 0) {

					Thread t = new Thread(() -> {
						try {
							String newCheckSum = FileTransferHelper.get32Hex(file);
							if (!json.getFileCheckSum().equals(newCheckSum)) {
								// контрольная сумма не сошлась - файл получен с ошибками
								throw new CheckSumException(file.getName());
							} else
								logger.debug("File "+file.toString()+" received successfully, checksum is valid.");
						} catch (Exception e) {
							logger.debug("Testing CheckSum of file "+ file.toString() +" failed: "+e.getMessage(), e);
							json.setPartsAmount(-1);
						}
					}); 
					t.setDaemon(true);
					service.submit(t);
				}

				// если условие истинно - файл получен полностью или возникла ошибка при его получении
				if (json.getPartsAmount() <= 0) {
					out = files.remove(fileID);
					
					if (json.getPartsAmount() < 0)
						Files.deleteIfExists(file.toPath());
				}
			}
		}
		return out;
	}
	
	/**
	 * Распарсить json
	 * @param in байты, полученные из сети
	 * @return объект StandardJsonQuery (json со служебной информацией)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private StandardJsonQuery decodeJson(ByteBuf in) throws IOException {
		
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
	private void recieveFile(ByteBuf in, int length, long pos, Path filePath) throws IOException {
		
		try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "rw")) {
			
			logger.debug("Receiving file/file part: "+filePath+", write from pos "+pos);
			raf.seek(pos);

			byte[] data = new byte[length];
			in.readBytes(data);

			raf.write(data);
		}
	}
}
