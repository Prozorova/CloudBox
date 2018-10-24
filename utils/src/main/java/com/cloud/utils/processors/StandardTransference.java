package com.cloud.utils.processors;

import com.cloud.utils.exep.IllegalDataException;

public class StandardTransference  {
	
	private final int msgCode;          // информация - файл или json
	
	private final int length;           // размер "посылки"
	private final long startPosition;   // если часть файла - начальная позиция
	private final String fileID;        // идентификатор файла
	
	private final byte[] data;          // сами данные, составляющие "посылку"
	
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
		
		return new StandardTransference(FileTransferHelper.CODE_FILE,
				                        data,
				                        data.length,
				                        startPosition,
				                        fileID);
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
		
		return new StandardTransference(FileTransferHelper.CODE_JSON,
				                        data,
				                        data.length,
				                        0l,
				                        null);
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
