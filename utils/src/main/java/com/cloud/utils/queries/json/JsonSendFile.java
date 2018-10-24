package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Служебная информация об отправляемом файле
 * @author prozorova 08.10.2018
 */
public class JsonSendFile extends StandardJsonQuery {
	
	public static final String  PARAM_NAME_FILENAME = "file_name";
	public static final String  PARAM_NAME_FILESIZE = "file_size";
	public static final String  PARAM_NAME_CHECKSUM = "check_sum";
	public static final String     PARAM_NAME_PARTS = "parts_amount";
	public static final String      PARAM_NAME_PATH = "file_path";
	public static final String PARAM_NAME_FILEOWNER = "file_owner";

	/**
	 * конструктор
	 * @param fileName имя файла
	 * @param fileSize размер (в байтах)
	 * @param fileCheckSum контрольная сумма
	 * @param filePath относительный путь к файлу
	 * @param fileOwner владелец файла
	 */
	public JsonSendFile(String fileName,
			            Long fileSize,
			            String fileCheckSum,
			            String filePath,
			            int parts) {
		
		super(QueryType.SEND_FILE,
			  new LinkedHashMap<String, String>(){
				{
					put(PARAM_NAME_FILENAME, fileName);
					put(PARAM_NAME_FILESIZE, fileSize.toString());
					put(PARAM_NAME_CHECKSUM, fileCheckSum);
					put(PARAM_NAME_PATH, filePath);
					put(PARAM_NAME_PARTS, String.valueOf(parts));
				}
			  });
	}

	/**
	 * Получить имя файла
	 * @return имя файла
	 */
	@JsonIgnore
	public String getFileName() {
		return this.getStandardParams().get(PARAM_NAME_FILENAME);
	}
	
	/**
	 * Получить размер файла
	 * @return размер файла (в байтах)
	 */
	@JsonIgnore
	public Long getFileSize() {
		return Long.parseLong(this.getStandardParams().get(PARAM_NAME_FILESIZE));
	}
	
	/**
	 * Получить контрольную сумму
	 * @return контрольная сумма
	 */
	@JsonIgnore
	public String getFileCheckSum() {
		return this.getStandardParams().get(PARAM_NAME_CHECKSUM);
	}
	
	/**
	 * Получить путь к файлу
	 * @return относительный путь к файлу
	 */
	@JsonIgnore
	public String getFilePath() {
		return this.getStandardParams().get(PARAM_NAME_PATH);
	}
	
	/**
	 * Получить количество частей, на которые разбит файл
	 * @return кол-во частей файла
	 */
	@JsonIgnore
	public int getPartsAmount() {
		return Integer.parseInt(this.getStandardParams().get(PARAM_NAME_PARTS));
	}
	
	/**
	 * Получить владельца файла
	 * @return идентификатор владельца файла
	 */
	@JsonIgnore
	public String getFileOwner() {
		return this.getStandardParams().get(PARAM_NAME_FILEOWNER);
	}
	
	/**
	 * Задать владельца файла
	 * @param owner
	 */
	public void setFileOwner(String owner) {
		this.getStandardParams().put(PARAM_NAME_FILEOWNER, owner);
	}
	
	/**
	 * Задать путь к файлу
	 * @param filePath путь к файлу
	 */
	public void setFilePath(String filePath) {
		this.getStandardParams().put(PARAM_NAME_PATH, filePath);
	}
	
	/**
	 * указать, на сколько частей разбивается файл
	 * @param parts кол-во частей файла
	 */
	public void setPartsAmount(int parts) {
		this.getStandardParams().put(PARAM_NAME_PARTS, String.valueOf(parts));
	}

}
