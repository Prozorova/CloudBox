package com.cloud;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cloud.fx.Controller;
import com.cloud.fx.SceneManager;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.queries.TransferMessage;

/**
 * Осуществляет отправку сообщений на сервер
 * @author prozorova 10.10.2018
 */
public class MessagesProcessor {
	
	private static final Logger logger = Logger.getLogger(MessagesProcessor.class);
	
	private static final MessagesProcessor PROCESSOR_INSTANCE = new MessagesProcessor();
	
	private Controller currentController;
	
	private MessagesProcessor() {}

	public static MessagesProcessor getProcessor() {
		return PROCESSOR_INSTANCE;
	}
	
	/**
	 * Отправить сообщение на сервер
	 * @param data сообщение для отправки
	 */
	public void sendData(TransferMessage data) {
		new Thread(() -> {
			try {
				CloudBoxClient.getCloudBoxClient(data, this);
	
			} catch (InterruptedException | IllegalDataException e) {
				logger.error("Server connection failed: " + e.getMessage(), e);
			} 
		}).run();
    }
	
	/**
	 * Обработать полученный от сервера результат аутентификации
	 * @param isAuthPass пройдена ли проверка
	 * @throws InterruptedException 
	 */
	public void login(boolean isAuthPass) throws InterruptedException {
		// TODO добавть сообщение о некорректных данных
		if (!isAuthPass) {
			logger.debug("Authentication is failed!");
			return;
		}
			
		try {
			// аутентификация пройдена, переключаем экран
			logger.debug("Authentication is passed");
			currentController = Controller.getSceneManager().changeScene(SceneManager.Scenes.MAIN_SCENE);
		} catch (IOException e) {
			logger.error("   Scene switching failed: " + e.getMessage(), e);
		}
	}
	

	public MessagesProcessor setController(Controller controller) {
		this.currentController = controller;
		return PROCESSOR_INSTANCE;
	}
}
