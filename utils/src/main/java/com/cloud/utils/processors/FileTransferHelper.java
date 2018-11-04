package com.cloud.utils.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

import org.apache.commons.codec.digest.DigestUtils;

import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * подготавливает объекты для отправки по сети
 * @author prozorova 02.11.2018
 */
public class FileTransferHelper {
	
    public static final int BUFFER_LEN = 100 * 1024 * 1024;   // 100 Mb
	
    public static final int CODE_JSON = 6348296;
    public static final int CODE_FILE = 9861904;
    
    // для работы с json
 	private static ObjectMapper mapper = new ObjectMapper();

    /**
     * подготовить файл или его часть для отправки
     * @param file файл, который отправляем
     * @param index номер части файла
     * @param fileID контрольная сумма
     * @return объект, подготовленный для передачи
     * @throws IllegalDataException
     * @throws IOException
     */
	public static StandardTransference prepareTransference(File file, int index, String fileID)
			throws IllegalDataException, IOException {
		
		StandardTransference message = null;
		
		if (file != null && file.exists() && file.length() > 0) {
			
			// файл можно передать целиком
			if (file.length() <= BUFFER_LEN)
				message = StandardTransference.getStandardTransference(Files.readAllBytes(file.toPath()),
						                                               0,
						                                               fileID);
			
			// если файл больше заданного размера и надо его дробить
			else {
				try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
					
					long currentPos = 1l * index * BUFFER_LEN;

					int length = (int) Math.min(BUFFER_LEN, file.length()-currentPos);
					byte[] buf = new byte[length];
					raf.seek(currentPos);
					raf.read(buf);

					message = StandardTransference.getStandardTransference(buf,
							                                               currentPos,
							                                               fileID);

				} catch (Throwable e) {
					//TODO
					e.printStackTrace();
				}
			}
			
		} else 
			throw new IllegalDataException("file is empty or doesn't exist, nothing to send");
		return message;
	}
	
	/**
	 * подготовить json для отправки
	 * @param json
	 * @return объект, подготовленный для передачи
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalDataException
	 */
	public static StandardTransference prepareTransference(StandardJsonQuery json) 
			throws IOException, IllegalDataException  {
		
		//TODO для тестирования - убрать
		mapper.writeValue(new FileOutputStream("111.json", false), json);
		
		byte[] jsonBytes = mapper.writeValueAsBytes(json);
		
		return StandardTransference.getStandardTransference(jsonBytes);
	}
	
	
	/**
	 * подсчет контрольной суммы файла
	 * @param file файл
	 * @return контрольная сумма
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String get32Hex(File file) throws FileNotFoundException, IOException  {
		return DigestUtils.md2Hex(new FileInputStream(file));
	}
}
