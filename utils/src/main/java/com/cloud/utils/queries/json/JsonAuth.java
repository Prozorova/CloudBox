package com.cloud.utils.queries.json;

import java.util.LinkedHashMap;

import com.cloud.utils.queries.StandardJsonQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Отправка запроса на аутентификацию
 * @author prozorova 04.10.2018
 */
public class JsonAuth extends StandardJsonQuery {
	
	// стандартные имена значений json 
	public static final String PARAM_NAME_LOGIN = "login";
	public static final String  PARAM_NAME_PASS = "password";
	
	/**
	 * Конструктор
	 * @param login логин
	 * @param pass пароль
	 */
	@SuppressWarnings("serial")
	public JsonAuth(QueryType type, String login, String pass) {
		super(type == QueryType.REG_DATA ? QueryType.REG_DATA : QueryType.AUTH_DATA,
			  new LinkedHashMap<String, String>(){
				{
					put(PARAM_NAME_LOGIN, login);
					put(PARAM_NAME_PASS, pass);
				}
			  });
	}
	
	/**
	 * Получить логин
	 * @return login
	 */
	@JsonIgnore
	public String getLogin() {
		return this.getStandardParams().get(PARAM_NAME_LOGIN);
	}
	
	/**
	 * Получить пароль
	 * @return password
	 */
	@JsonIgnore
	public String getPassword() {
		return this.getStandardParams().get(PARAM_NAME_PASS);
	}

}
