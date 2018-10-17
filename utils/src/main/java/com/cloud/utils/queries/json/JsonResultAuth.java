package com.cloud.utils.queries.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Ответ от сервера на запрос аутентификации
 * @author prozorova 04.10.2018
 */
public class JsonResultAuth extends StandardJsonQuery {
	
	public static final String PARAM_NAME_ANSWER = "answer";
	public static final String PARAM_NAME_REJECT_REASON = "reason";
	public static final String PARAM_NAME_ROOT_FILES = "filesInRoot";

	/**
	 * конструктор положительного ответа
	 * @param список файлов в корневой папке пользователя
	 * @throws IOException 
	 */
	@SuppressWarnings("serial")
	public JsonResultAuth(Set<String> files) throws IOException {
		super(QueryType.AUTH_RESULT, 
				new LinkedHashMap<String, String>() {
			{
				put(PARAM_NAME_ANSWER, "true");
			}
		});
		this.setParamsWithSet(new LinkedHashMap<String, Set<String>>() {
			{
				put(PARAM_NAME_ROOT_FILES, files); 
			}
		});
	}
	
	/**
	 * конструктор в случае отрицательного ответа сервера
	 * @param reason причина отказа
	 */
	@SuppressWarnings("serial")
	public  JsonResultAuth(String reason) {
		super(QueryType.AUTH_RESULT, 
				new LinkedHashMap<String, String>() {
			{
				put(PARAM_NAME_ANSWER, "false");
				put(PARAM_NAME_REJECT_REASON, reason);
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
	
	/**
	 * получить список файлов в корневом каталоге пользователя
	 * @return список файлов
	 */
	@JsonIgnore
	public Set<String> getFiles() {
		return this.getParamsWithSet() == null ? null : this.getParamsWithSet().get(PARAM_NAME_ROOT_FILES);
	}
	
	/**
	 * получить причину отказа
	 * @return причина
	 */
	@JsonIgnore
	public String getReason() {
		return this.getStandardParams().get(PARAM_NAME_REJECT_REASON);
	}
	
}
