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

public class FileTransferHelper {
	
    public static final int BUFFER_LEN = 100 * 1024 * 1024;   // 10 Mb
	
    public static final int CODE_JSON = 6348296;
    public static final int CODE_FILE = 9861904;
    
    // для работы с json
 	private static ObjectMapper mapper = new ObjectMapper();

    
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
	
	public static StandardTransference prepareTransference(StandardJsonQuery json) 
			throws JsonGenerationException, JsonMappingException, FileNotFoundException, 
			IOException, IllegalDataException  {
		
		//TODO для тестирования - убрать
		mapper.writeValue(new FileOutputStream("111.json", false), json);
		
		byte[] jsonBytes = mapper.writeValueAsBytes(json);
		
		return StandardTransference.getStandardTransference(jsonBytes);
		
	}
	
	public static String get32Hex(File file) throws FileNotFoundException, IOException  {
		return DigestUtils.md2Hex(new FileInputStream(file));
	}
}
