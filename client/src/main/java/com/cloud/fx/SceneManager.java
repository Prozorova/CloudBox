package com.cloud.fx;

import java.io.IOException;

import com.cloud.MainClientGUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Менеджер переключения экранов
 * @author prozorova 05.10.2018
 */
public class SceneManager {
	
	 public enum Scenes {AUTH,         // экран авторизации
		                 MAIN_SCENE,   // основной экран работы приложения
		                 LOADING}      // TODO экран загрузки       ????
	 
	 private Stage primaryStage;
	 
	 private String mainCssFile;
	 
	 private FXMLLoader authLoader;
	 private Scene      authScene;
	 
	 private FXMLLoader mainLoader;
	 private Scene      mainScene;
	 
	 private FXMLLoader loadLoader;
	 private Scene      loadScene;
	 
	 private Scene newScene = null;
	 
	 public SceneManager (MainClientGUI app, Stage primaryStage) throws IOException {
		 
		 this.mainCssFile = app.getClass().getResource("/application.css").toExternalForm();
		 this.authLoader  = new FXMLLoader(getClass().getResource("/CloudBoxAuth.fxml"));
		 this.mainLoader  = new FXMLLoader(getClass().getResource("/CloudBoxGUI.fxml"));
//		 this.loadURL     = app.getClass().getResource("");
		 
		 this.primaryStage = primaryStage;
		 
		 Controller.setSceneManager(this);
	 }
	 
	 public Controller changeScene (Scenes scene) throws IOException {
		 Controller controller = null;

		 switch (scene) {
			case AUTH:
				newScene = getScene(authLoader, authScene);
				controller = authLoader.getController();
				break;
			case MAIN_SCENE:
				newScene = getScene(mainLoader, mainScene);
				controller = mainLoader.getController();
				break;
			case LOADING:
				newScene = getScene(loadLoader, loadScene);
				controller = loadLoader.getController();
				break;
				
		 }
		Platform.runLater(() -> {
			primaryStage.setScene(newScene);
			primaryStage.show();
		});
		 
		 return controller;
	 }
	 
	 private Scene getScene(FXMLLoader loader, Scene scene) throws IOException {
		
		if (scene == null) {
			Parent root = loader.load();
			scene = new Scene(root, 1000, 700);
			scene.getStylesheets().add(mainCssFile);
		}
		return scene;
	 }
}
