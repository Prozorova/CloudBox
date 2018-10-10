package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Ответ от сервера на запрос аутентификации
 * @author prozorova 04.10.2018
 */
public class JsonResultAuth extends StandardJsonQuery {
	
	public static final String PARAM_NAME_ANSWER = "answer";

	/**
	 * конструктор
	 * @param answer результат аутентификации
	 */
	public JsonResultAuth(boolean answer) {
		super(QueryType.AUTH_RESULT, 
			  new LinkedHashMap<String, String>() {
				{
					put(PARAM_NAME_ANSWER, Boolean.toString(answer));
				}
			  });
	}

	/**
	 * Получить результат аутентификации
	 * @return результат аутентификации
	 */
	@JsonIgnore
	public boolean getAuthResult() {
		return this.getStandardParams().get(PARAM_NAME_ANSWER).equals("true");
	}
}
