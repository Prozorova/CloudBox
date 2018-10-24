package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JsonSimpleMessage extends StandardJsonQuery {

	// стандартные имена значений json
	public static final String PARAM_NAME_MESSAGE = "message";
	public static final String PARAM_NAME_DISCONNECT = "isDisconnected";

	/**
	 * Конструктор с возможностью отключения
	 * @param login сообщение
	 */
	@SuppressWarnings("serial")
	public JsonSimpleMessage(String message, boolean disconnect) {
		super(QueryType.MESSAGE,
				new LinkedHashMap<String, String>(){
			{
				put(PARAM_NAME_MESSAGE, message);
				put(PARAM_NAME_DISCONNECT, Boolean.toString(disconnect));
			}
		});
	}
	
	/**
	 * Обычное сообщение
	 * @param login сообщение
	 */
	@SuppressWarnings("serial")
	public JsonSimpleMessage(String message) {
		super(QueryType.MESSAGE,
				new LinkedHashMap<String, String>(){
			{
				put(PARAM_NAME_MESSAGE, message);
				put(PARAM_NAME_DISCONNECT, "false");
			}
		});
	}

	/**
	 * Получить сообщение
	 * @return сообщение
	 */
	@JsonIgnore
	public String getMessage() {
		return this.getStandardParams().get(PARAM_NAME_MESSAGE);
	}
	
	/**
	 * Будет ли дисконнект
	 * @return результат
	 */
	@JsonIgnore
	public boolean getIsDisconnected() {
		return this.getStandardParams().get(PARAM_NAME_DISCONNECT).equals("true");
	}
}
