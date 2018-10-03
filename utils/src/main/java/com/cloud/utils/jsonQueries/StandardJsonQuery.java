package com.cloud.utils.jsonQueries;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
 
/**
 * Содержит все параметры запросов/ответов,
 * используется для работы с json
 * @author prozorova
 */
public class StandardJsonQuery  {
	
	// TODO список всех типов запросов/ответов
	public enum QueryType {
					AUTH_DATA	   // данные аутентификации
		};
	
	private String queryType;
	
	// обычные параметры название-значение
	@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
	private Map<String, String> standardParams;
	
	// параметры, задающиеся списком (напр. для отправки списка файлов пользователю)
	@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
	private Map<String, Set<String>> paramsWithSet;
	
	public StandardJsonQuery() {
	}
	
	public StandardJsonQuery(QueryType queryType, 
			                 Map<String, String> parameters, 
			                 Map<String, Set<String>> paramsWithSet) {
		this.queryType = queryType.toString();
		this.standardParams = parameters;
		this.paramsWithSet = paramsWithSet;
	}
	
	public StandardJsonQuery(QueryType queryType, 
			Map<String, String> parameters) {
		this(queryType, parameters, null);
	}

	
	// ************ геттеры / сеттеры для корректной работы jackson ************ \\

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public Map<String, String> getStandartParams() {
		return standardParams;
	}

	public void setStandartParams(Map<String, String> standartParams) {
		this.standardParams = standartParams;
	}

	public Map<String, Set<String>> getParamsWithSet() {
		return paramsWithSet;
	}

	public void setParamsWithSet(Map<String, Set<String>> paramsWithSet) {
		this.paramsWithSet = paramsWithSet;
	}
}
