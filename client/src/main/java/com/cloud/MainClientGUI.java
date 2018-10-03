package com.cloud;
	
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class MainClientGUI extends Application {
	
	private static final Logger logger = Logger.getLogger(MainClientGUI.class);
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/CloudBoxAuth.fxml"));
			Scene scene = new Scene(root,1000,700);
			primaryStage.setTitle("CloudBox Client");
			primaryStage.setMaxWidth(1000);
			primaryStage.getIcons().add(new Image(getResource("/Icons/icon-16x16.png").toExternalForm()));
			String css = this.getClass().getResource("/application.css").toExternalForm();
			scene.getStylesheets().add(css);
			primaryStage.setScene(scene);		
			primaryStage.show();
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
