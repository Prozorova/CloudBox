package com.cloud.server;

import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonResultAuth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * Осуществляет регистрацию и аутентификацию пользователей
 * @author prozorova 10.10.2018
 */
public class AuthManager {
	
	private static final AuthManager INSTANCE = new AuthManager();
	
	private AuthManager() {}
	
	public static AuthManager getAuthManagerInstance() {
		return INSTANCE;
	}
	
	/**
	 * Ответить на запрос аутентификации
	 * @param auth данные аутентификации
	 * @param channel канал соединения с пользователем
	 */
	public void acceptAuth(JsonAuth auth, Channel channel) {
		String login = auth.getLogin();
		String pass = auth.getPassword();
		
		// формирование ответа
		JsonResultAuth jsonResponse = new JsonResultAuth(checkPassword(login, pass));
		
		channel.writeAndFlush(new TransferMessage(jsonResponse)).addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * Проверка данных аутентификации - осуществляется через БД
	 * @param login логин
	 * @param pass пароль
	 * @return результат проверки
	 */
	private boolean checkPassword(String login, String pass) {
		// TODO
		return true;
    }
}
