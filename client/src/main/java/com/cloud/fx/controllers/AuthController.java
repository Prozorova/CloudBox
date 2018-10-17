package com.cloud.fx.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.cloud.fx.Controller;
import com.cloud.fx.MessagesProcessor;
import com.cloud.utils.queries.StandardJsonQuery.QueryType;
import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonAuth;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Осуществляет управление экраном аутентификации
 * @author prozorova 05.10.2018
 */
public class AuthController extends Controller implements Initializable {
	
	private static final Logger logger = Logger.getLogger(AuthController.class);

	@FXML Button btnLogin;                 // логин
	@FXML Button btnSendAuth;              // отправить новые данные для аутентификации
	@FXML Button btnReg;                   // зарегистрировать нового пользователя
	@FXML Button btnLoginWithoutReg;       // вернуться к аутентификации
	@FXML Label labelPassConfirm;
	@FXML TextField textFieldPassConfirm;  // подтверждение пароля
	@FXML TextField textFieldLogin;        // логин
	@FXML TextField textFieldPass;         // пароль

	@FXML BorderPane mainBorderPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	
	/**
	 * войти с указанными данными
	 */
	@FXML public void btnLoginClickMeReaction() {
		
		JsonAuth jsonQuery = new JsonAuth(QueryType.AUTH_DATA, textFieldLogin.getText(), textFieldPass.getText());
		
		MessagesProcessor.getProcessor().setController(this).sendData(new TransferMessage(jsonQuery));
		
		clearTextFields();
		
	}

	/**
	 * отправить новые данные для аутентификации
	 */
	@FXML public void btnbtnSendAuthClickMeReaction() {
		
		if (textFieldPass.getText().equals(textFieldPassConfirm.getText())) {
			
			JsonAuth jsonQuery = new JsonAuth(QueryType.REG_DATA, textFieldLogin.getText(), textFieldPass.getText());
		
			MessagesProcessor.getProcessor().setController(this).sendData(new TransferMessage(jsonQuery));
			
			clearTextFields();
		} else {
			textFieldPass.setStyle("-fx-text-inner-color: red;");
			textFieldPassConfirm.setStyle("-fx-text-inner-color: red;");
		}
	}

	/**
	 * зарегистрировать нового пользователя
	 */
	@FXML public void btnbtnRegClickMeReaction() {
		
		switchAvailability(false, btnLogin, 
				                  btnReg);
		
		switchAvailability(true,  labelPassConfirm, 
				                  textFieldPassConfirm,
				                  btnSendAuth,
				                  btnLoginWithoutReg);
		
		clearTextFields();
	}

	/**
	 * вернуться к аутентификации
	 */
	@FXML public void btnBackToLoginClickMeReaction() {

		switchAvailability(false, labelPassConfirm, 
                                  textFieldPassConfirm,
                                  btnSendAuth,
                                  btnLoginWithoutReg);
		
		switchAvailability(true,  btnLogin, 
                                  btnReg);
		
		clearTextFields();
	}
	
	private void clearTextFields() {
		textFieldLogin.clear();
		textFieldLogin.requestFocus();
		textFieldPass.clear();
		textFieldPassConfirm.clear();
		
		textFieldPass.setStyle("-fx-text-inner-color: black;");
		textFieldPassConfirm.setStyle("-fx-text-inner-color: black;");
	}


	/**
	 * переключает видимость и доступность элементов интерфейса
	 * @param bool флаг переключения
	 * @param nodes массив элементов
	 */
	private void switchAvailability(boolean bool, Node...nodes) {
		for (Node node : nodes) {
			node.setManaged(bool);
			node.setVisible(bool);
		}
	}

	
}
