package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Ответ с подтверждением
 * @author prozorova 10.10.2018
 */
public class JsonConfirm extends StandardJsonQuery {
	
	public static final String PARAM_NAME_ANSWER = "confirmation";

	/**
	 * конструктор
	 * @param answer результат
	 */
	public JsonConfirm(String answer) {
		super(QueryType.CONFIRMATION, 
			  new LinkedHashMap<String, String>() {
				{
					put(PARAM_NAME_ANSWER, answer);
				}
			  });
	}

	/**
	 * Получить результат (подтверждение)
	 */
	@JsonIgnore
	public String getConfirmation() {
		return this.getStandardParams().get(PARAM_NAME_ANSWER);
	}
}