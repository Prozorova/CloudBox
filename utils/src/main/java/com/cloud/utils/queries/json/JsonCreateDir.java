package com.cloud.utils.queries.json;


import java.util.LinkedHashMap;

import com.cloud.utils.queries.PathContainer;
import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JsonCreateDir extends StandardJsonQuery implements PathContainer{
	
	// стандартные имена значений json 
	public static final String PARAM_NAME_PATH        = "dirPath";
	public static final String PARAM_NAME_NEW_FOLDER  = "newFolderName";

	/**
	 * Конструктор
	 * @param dirPath путь к папке, где будет создана новая
	 * @param newFolderName имя новой папки
	 */
	@SuppressWarnings("serial")
	public JsonCreateDir(String dirPath, String newFolderName) {
		super(QueryType.CREATE_DIR,
				new LinkedHashMap<String, String>(){
			{
				put(PARAM_NAME_PATH, dirPath);
				put(PARAM_NAME_NEW_FOLDER, newFolderName);
			}
		});
	}

	/**
	 * Получить путь к папке, где будет создана новая
	 * @return путь
	 */
	@JsonIgnore
	@Override
	public String getFilePath() {
		return this.getStandardParams().get(PARAM_NAME_PATH);
	}
	
	/**
	 * Задать путь к папке, где будет создана новая
	 * @param path путь
	 */
	@JsonIgnore
	@Override
	public void setFilePath(String path) {
		this.getStandardParams().put(PARAM_NAME_PATH, path);
	}

	/**
	 * Получить имя новой папки
	 * @return имя новой папки
	 */
	@JsonIgnore
	public String getNewFolderName() {
		return this.getStandardParams().get(PARAM_NAME_NEW_FOLDER);
	}

}
