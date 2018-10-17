package com.cloud.utils.queries;

import java.io.File;

/**
 * Стандартное сообщение, может содержать только служебную
 * информацию или еще передаваемый  файл
 * @author prozorova 04.10.2018
 */
public class TransferMessage {
	
	// вся служебная информация + запросы/ответы
	private StandardJsonQuery jsonQuery;
	// передаваемый файл
	private File file = null;
	
	public static final String DIVIDER = " ## ";
	
	/**
	 * конструктор для передачи только служебной информации
	 * @param jsonQuery
	 */
	public TransferMessage (StandardJsonQuery jsonQuery) {
		this.jsonQuery = jsonQuery;
	}

	/**
	 * Получить json из сообщения
	 * @return StandardJsonQuery
	 */
	public StandardJsonQuery getJsonQuery() {
		return jsonQuery;
	}

	/**
	 * Получить передаваемый файл
	 * @return File
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Добавить файл для передачи
	 * @param file файл
	 * @return передаваемый объект
	 */
	public TransferMessage addFile(File file) {
		this.file = file;
		return this;
	}

}
