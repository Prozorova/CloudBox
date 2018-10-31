package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.PathContainer;
import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Отправка запроса на переименование файла или папки
 * по факту, сюда можно отнести и их перемещение
 * @author prozorova 25.10.2018
 */
public class JsonRename extends StandardJsonQuery implements PathContainer{

	// стандартные имена значений json 
	public static final String PARAM_NAME_FILEPATH = "filePath";
	public static final String PARAM_NAME_NEWNAME  = "newFileName";

	/**
	 * Конструктор
	 * @param filePath путь к файлу или папке на переименование
	 * @param newName новое имя (или путь)
	 */
	@SuppressWarnings("serial")
	public JsonRename(String filePath, String newName) {
		super(QueryType.RENAME,
				new LinkedHashMap<String, String>(){
			{
				put(PARAM_NAME_FILEPATH, filePath);
				put(PARAM_NAME_NEWNAME, newName);
			}
		});
	}

	/**
	 * Получить путь к файлу или папке на переименование
	 * @return путь
	 */
	@JsonIgnore
	@Override
	public String getFilePath() {
		return this.getStandardParams().get(PARAM_NAME_FILEPATH);
	}
	
	/**
	 * Получить новое имя (путь) файла
	 * @return новое имя
	 */
	@JsonIgnore
	public String getNewFileName() {
		return this.getStandardParams().get(PARAM_NAME_NEWNAME);
	}
	
	/**
	 * Задать текущий путь к файлу/папке
	 * @param currentPath текущий путь
	 */
	@JsonIgnore
	@Override
	public void setFilePath(String currentPath) {
		this.getStandardParams().put(PARAM_NAME_FILEPATH, currentPath);
	}
	
	/**
	 * Задать новый путь к файлу/папке
	 * @param newPath новый путь
	 */
	@JsonIgnore
	public void setNewFileName(String newPath) {
		this.getStandardParams().put(PARAM_NAME_NEWNAME, newPath);
	}

}
