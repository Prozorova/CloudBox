package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.PathContainer;
import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JsonDelete extends StandardJsonQuery implements PathContainer{

	// стандартные имена значений json 
	public static final String PARAM_NAME_FILEPATH = "filePath";
	
	/**
	 * Конструктор
	 * @param filePath путь к файлу или папке на удаление
	 */
	@SuppressWarnings("serial")
	public JsonDelete(String filePath) {
		super(QueryType.DELETE,
			  new LinkedHashMap<String, String>(){
				{
					put(PARAM_NAME_FILEPATH, filePath);
				}
			  });
	}
	
	/**
	 * Получить путь к файлу или папке на удаление
	 * @return путь
	 */
	@JsonIgnore
	@Override
	public String getFilePath() {
		return this.getStandardParams().get(PARAM_NAME_FILEPATH);
	}
		
	/**
	 * Задать путь к файлу или папке на удаление
	 * @param filePath путь
	 */
	@JsonIgnore
	@Override
	public void setFilePath(String filePath) {
		this.getStandardParams().put(PARAM_NAME_FILEPATH, filePath);
	}
}
