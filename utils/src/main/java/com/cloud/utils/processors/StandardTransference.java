package com.cloud.utils.processors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.cloud.utils.exep.IllegalDataException;

/**
 * Стандартизирует объекты для последующей передачи по сети
 * @author prozorova 02.11.2018
 */
public class StandardTransference  {
	
	private final static BlockingQueue<StandardTransference> OBJECTS_POOL = new LinkedBlockingQueue<>();
	
	private int msgCode;          // информация - файл или json
	
	private int length;           // размер "посылки"
	private long startPosition;   // если часть файла - начальная позиция
	private String fileID;        // идентификатор файла
	
	private byte[] data;          // сами данные, составляющие "посылку"
	
	private boolean isFree = false;     // можно ли переиспользовать объект
	
	private StandardTransference(int msgCode, byte[] data, int length, long startPosition, String fileID) {

		this.msgCode       = msgCode;
		this.data          = data;
		this.length        = length;
		this.startPosition = startPosition;
		this.fileID        = fileID;
		
	}
	
	/**
	 * для упаковки файла или части файла
	 * @param data набор байт
	 * @param startPosition начальная позиция части файла
	 * @param fileID контрольная информация файла для его идентификации
	 * @return объект для отправки
	 * @throws IllegalDataException
	 */
	public static StandardTransference getStandardTransference(byte[] data, long startPosition, String fileID) 
			throws IllegalDataException {
		
		if (data == null || data.length == 0)
			throw new IllegalDataException("data is empty");
		if (startPosition < 0)
			throw new IllegalDataException("position of part in file " + startPosition);
		if (fileID.length() != 32)
			throw new IllegalDataException("control information check failed");
		
		return getStandardTransference(FileTransferHelper.CODE_FILE, data, startPosition, fileID);
	}
	
	/**
	 * для упаковки json
	 * @param data json (набор байт)
	 * @return объект для отправки
	 * @throws IllegalDataException 
	 */
	public static StandardTransference getStandardTransference(byte[] data) throws IllegalDataException {
		
		if (data == null || data.length == 0)
			throw new IllegalDataException("data is empty");
		
		return getStandardTransference(FileTransferHelper.CODE_JSON, data, 0l, null);
	}
	
	/**
	 * вспомогательный метод для создания нового или переиспользования существующего в пуле объекта
	 */
	private static StandardTransference getStandardTransference(int code, byte[] data, long startPosition, String fileID) {
		
		StandardTransference transference;
				
		// если нет свободных объектов - создаем новый
		if (OBJECTS_POOL.size() == 0 || !OBJECTS_POOL.peek().isFree)
			transference = new StandardTransference(code,
                                                    data,
								                    data.length,
								                    startPosition,
								                    fileID);
		// если есть - переиспользуем
		else {
			transference = OBJECTS_POOL.poll();

			transference.msgCode       = code;
			transference.data          = data;
			transference.length        = data.length;
			transference.startPosition = startPosition;
			transference.fileID        = fileID;
			transference.isFree        = false;
		}
		
		return transference;
	}
	
	/**
	 * освободить объект после его использования
	 */
	public void freeObject() {
		this.isFree = true;
		OBJECTS_POOL.add(this);
	}
	
	public int getTransferMessageCode() {
		return msgCode;
	}

	public int getLength() {
		return length;
	}

	public long getStartPosition() {
		return startPosition;
	}

	public String getFileID() {
		return fileID;
	}

	public byte[] getData() {
		return data;
	}

}
