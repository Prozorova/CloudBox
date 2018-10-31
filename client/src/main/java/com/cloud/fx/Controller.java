package com.cloud.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * родительский класс для контроллеров
 * @author prozorova 31.10.2018
 */
public abstract class Controller {
	
	// менеджер переключения экранов
	private static SceneManager manager;
	
	// список файлов, полученный от сервера
	private static Set<String> serverFiles;
	
	// для регулирования обработки - на сервере или на клиенте
	public enum FilesSource {SERVER, CLIENT};
	
	// список файлов на получение - это если был запрос на загрузку файла с сервера
	private static Map<String, String> files = new HashMap<>();
	
	/**
	 * вывести информационное сообщение
	 * @param title заголовок, тема
	 * @param msg сообщение
	 */
	public static void throwAlertMessage(String title, String msg) {
		Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
		});
	}
	
	/**
	 * запомнить, куда загрузить файл, который мы запросили на сервере
	 * @param pathOnServer путь, по которому он находится на сервере (как мы его видим)
	 * @param pathOnClient путь, куда этот файл необходимо загрузить на клиентской машине
	 */
	protected void addFile(String pathOnServer, String pathOnClient) {
		files.put(pathOnServer, pathOnClient);
	}
	
	/**
	 * получить путь, куда загружать файл
	 * @param pathOnServer путь на сервере - должен возвращаться в ответе от сервера в неизменном виде
	 * @return путь, куда загружать файл
	 */
	public static String getFilePath(String pathOnServer) {
		return files.get(pathOnServer);
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
	
    public void refresh(FilesSource source) {
	}

}
