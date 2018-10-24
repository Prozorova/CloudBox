package com.cloud.server;

import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.exep.UserSQLException;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.StandardJsonQuery.QueryType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonResultAuth;
import com.cloud.utils.queries.json.JsonSimpleMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * Осуществляет регистрацию и аутентификацию пользователей
 * @author prozorova 10.10.2018
 */
public class AuthManager {
	
	private static final Logger logger = Logger.getLogger(AuthManager.class);
	
	// список пользователей в сети
	private static final ConcurrentMap<Channel, String> currentUsers = new ConcurrentHashMap<>();
	
	/**
	 * Ответить на запрос аутентификации
	 * @param auth данные аутентификации
	 * @param channel канал соединения с пользователем
	 */
	public void acceptAuth(JsonAuth auth, FilesProcessor processor, Channel channel) {
		String login = auth.getLogin();
		String pass = auth.getPassword();
		
		JsonResultAuth jsonResponse = null;
		
		try {
			//если пришел запрос аутентификации
			if (auth.getQueryType() == QueryType.AUTH_DATA) {
				
				// если такой пользователь уже в сети
				if (currentUsers.containsValue(login)) {
					throw new UserSQLException("This user is already online");
				}
				
				Path authResultPrivateBox = this.getUserFiles(login, pass);
				jsonResponse = new JsonResultAuth(processor.gatherFilesFromDir(authResultPrivateBox));
			}	

			// если запрос на регистрацию нового пользователя
			else if (auth.getQueryType() == QueryType.REG_DATA) {
				Path regResultPrivateBox = this.registerUser(login, pass);
				if (regResultPrivateBox == null)
					throw new UserSQLException("This username already exists");
				jsonResponse = new JsonResultAuth(processor.gatherFilesFromDir(regResultPrivateBox));
			}
		} catch (IOException e) {
			logger.error("Gathering files in local user "+login+" directory failed: " + e.getMessage(), e);
		} catch (UserSQLException e) {
			jsonResponse = new JsonResultAuth(e.getMessage());
		} finally {
			if (jsonResponse == null) 
				jsonResponse = new JsonResultAuth("Server error");
		}
		
		try {
			if (jsonResponse.getAuthResult()) {
				currentUsers.put(channel, login);
				channel.writeAndFlush(FileTransferHelper.prepareTransference(jsonResponse));
			} else
				channel.writeAndFlush(FileTransferHelper.prepareTransference(jsonResponse)).addListener(ChannelFutureListener.CLOSE);
		} catch (IllegalDataException | IOException e) {
			logger.error("Transferece preparing failed: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Проверка данных аутентификации и получение пути к ящику пользователя
	 * @param login логин
	 * @param pass пароль
	 * @return путь к личному ящику пользователя
	 * @throws UserSQLException 
	 */
	private Path getUserFiles(String login, String pass) throws UserSQLException {
		try {
			return Paths.get(DatabaseQueriesProcessor.getInstance().checkUserAuth(login, pass));
		} catch (SQLException e) {
			logger.debug("Authentification ("+login+") is failed (DB problem): " + e.getMessage(), e);
			if (e instanceof UserSQLException)
				throw (UserSQLException)e;
		}
		return null;
    }

	/**
	 * Регистрация нового пользователя
	 * @param login логин
	 * @param pass пароль
	 * @return путь к личному ящику пользователя
	 */
	private Path registerUser(String login, String pass) {
		try {
			if (DatabaseQueriesProcessor.getInstance().addNewUser(login, pass)) {
				try {
					return Paths.get(DatabaseQueriesProcessor.getInstance().getPath(login));
				} catch (SQLException e) {
					logger.error("Getting path to user box "+login+" failed: " +e.getMessage(), e);
				}
			}
		} catch (SQLException e) {
			logger.error("Registation ("+login+") is failed (DB problem): " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Registation ("+login+") is failed (problem with creating private dir): " + e.getMessage(), e);
		}
		return null;
	}
	
	public static String getUser(Channel channel) {
		return currentUsers.get(channel);
	}
	
	public void removeFromMap(Channel channel) {
		currentUsers.remove(channel);
	}
	
	/**
	 * оповещение пользователей о прекращении работы сервера
	 */
	static void disconnectFromServer() throws Exception {
		for (Channel user : currentUsers.keySet()) {
			StandardJsonQuery json = new JsonSimpleMessage("Connection with server lost", true);
			user.writeAndFlush(FileTransferHelper.prepareTransference(json));
		}
		
		currentUsers.clear();
	}
}
