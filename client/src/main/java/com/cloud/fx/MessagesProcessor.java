package com.cloud.fx;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cloud.CloudBoxClient;
import com.cloud.fx.controllers.MainSceneController;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.queries.TransferMessage;

import javafx.application.Platform;

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
		Thread t = new Thread(() -> {
			try {
				CloudBoxClient.getCloudBoxClient(data, this);
	
			} catch (InterruptedException | IllegalDataException e) {
				logger.error("Server connection failed: " + e.getMessage(), e);
			} 
		});
		t.setDaemon(true); t.start();
    }
	
	/**
	 * Обработать полученный от сервера результат аутентификации
	 * @param isAuthPass пройдена ли проверка
	 * @param files список файлов в корневом каталоге пользователя
	 * @param reason 
	 * @throws InterruptedException 
	 */
	public void login(boolean isAuthPass, Set<String> files, String reason) throws InterruptedException {
		
		if (!isAuthPass) {
			logger.debug("Authentication is failed: " + reason);
			currentController.throwAlertMessage("Authorization failed", reason);
			return;
		}
			
		try {
			// аутентификация пройдена, переключаем экран
			logger.debug("Authentication is passed");
			Controller.setServerFilesSet(files);
//			Controller.setLogin(user);
			currentController = Controller.getSceneManager().changeScene(SceneManager.Scenes.MAIN_SCENE);
			
		} catch (IOException e) {
			logger.error("   Scene switching failed: " + e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param files
	 */
	public void refreshFilesOnServer(Set<String> files) {
		Controller.setServerFilesSet(files);
		Platform.runLater(() -> {
			currentController.refresh();
		});
	}

	public MessagesProcessor setController(Controller controller) {
		this.currentController = controller;
		return PROCESSOR_INSTANCE;
	}
	
	public void showAlert(String title, String msg) {
		currentController.throwAlertMessage(title, msg);
	}
}
