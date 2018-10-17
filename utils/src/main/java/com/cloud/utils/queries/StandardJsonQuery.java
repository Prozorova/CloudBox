package com.cloud.utils.queries;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
 
/**
 * Содержит все параметры запросов/ответов,
 * используется для работы с json
 * @author prozorova
 */
@JsonDeserialize(using = StandardQueryCustomDeserializer.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class StandardJsonQuery  {
	
	// TODO список всех типов запросов/ответов
	public enum QueryType {
					AUTH_DATA,	   // данные аутентификации
					AUTH_RESULT,   // ответ сервера на запрос аутентификации
					SEND_FILE,     // передать файл
					CONFIRMATION,  // подтверждение, напр. о получении файла
					REG_DATA       // регистрация нового пользователя
		};
	
	private QueryType queryType;
	
	// обычные параметры название-значение
	@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
	private Map<String, String> standardParams;
	
	// параметры, задающиеся списком (напр. для отправки списка файлов пользователю)
	@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
	private Map<String, Set<String>> paramsWithSet;
	
	public StandardJsonQuery() {
	}
	
	protected StandardJsonQuery(QueryType queryType,
			                    Map<String, String> parameters) {
		this(queryType, parameters, null);
	}
	
	protected StandardJsonQuery(QueryType queryType, 
			                    Map<String, String> parameters,
			                    Map<String, Set<String>> paramsWithSet) {
		this.queryType = queryType;
		this.standardParams = parameters;
		this.paramsWithSet = paramsWithSet;
	}

	
	// ************ геттеры / сеттеры ************ \\

	public QueryType getQueryType() {
		return queryType;
	}

	protected void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	protected Map<String, String> getStandardParams() {
		return standardParams;
	}

	protected void setStandardParams(Map<String, String> standardParams) {
		this.standardParams = standardParams;
	}

	protected Map<String, Set<String>> getParamsWithSet() {
		return paramsWithSet;
	}

	protected void setParamsWithSet(Map<String, Set<String>> paramsWithSet) {
		this.paramsWithSet = paramsWithSet;
	}
}
