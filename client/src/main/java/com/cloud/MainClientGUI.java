package com.cloud;
	
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.cloud.fx.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

/**
 * Класс запускает клиентскую часть приложения
 * @author prozorova 05.10.2018
 */
public class MainClientGUI extends Application {
	
	private static final Logger logger = Logger.getLogger(MainClientGUI.class);
	
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("CloudBox Client");
			primaryStage.setMaxWidth(1000);
			primaryStage.getIcons().add(new Image(getResource("/Icons/icon-16x16.png").toExternalForm()));
			
			primaryStage.setOnCloseRequest(event -> {
				System.exit(0);
			});
			
			// создание экземпляра SceneManager и показ экрана авторизации
			new SceneManager(this, primaryStage).changeScene(SceneManager.Scenes.AUTH);
				
		} catch(IOException e) {
			logger.error("Loading GUI error: " + e.getMessage(), e);;
		}
	}
	
	private URL getResource(String name) {
		return getClass().getResource(name);
	}
	
	public static void main(String[] args) {
		
		launch(args);
	}
}
