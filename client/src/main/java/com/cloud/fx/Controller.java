package com.cloud.fx;

import java.util.Set;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public abstract class Controller {
	
	// менеджер переключения экранов
	private static SceneManager manager;
	private static Set<String> serverFiles;
	private static String login;
	
	
	public static void throwAlertMessage(String title, String msg) {
		Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
		});
	}
	
	static void setLogin(String user) {
		login = user;
	}
	
	public static SceneManager getSceneManager() {
		return manager;
	}
	
	static void setSceneManager(SceneManager sceneManager) {
		manager = sceneManager;
	}
	
	static void setServerFilesSet(Set<String> files) {
		serverFiles = files;
	}
	
	protected Set<String> getFilesOnServer() {
		return serverFiles;
	}
	
	protected String getLogin() {
		return login;
	}
	
    public void refresh() {
	}

}
