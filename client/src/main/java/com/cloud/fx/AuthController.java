package com.cloud.fx;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.cloud.CloudBoxClient;
import com.cloud.utils.jsonQueries.StandardJsonQuery;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AuthController implements Initializable {
	
	private static final Logger logger = Logger.getLogger(AuthController.class);

	@FXML Button btnLogin;                 // логин
	@FXML Button btnSendAuth;              // отправить новые данные для аутентификации
	@FXML Button btnReg;                   // зарегистрировать нового пользователя
	@FXML Button btnLoginWithoutReg;       // вернуться к аутентификации
	@FXML Label labelPassConfirm;
	@FXML TextField textFieldPassConfirm;  // подтверждение пароля
	@FXML TextField textFieldLogin;        // логин
	@FXML TextField textFieldPass;         // пароль

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	
	/**
	 * войти с указанными данными
	 */
	@FXML public void btnLoginClickMeReaction() {
		
		StandardJsonQuery jsonQuary = new StandardJsonQuery(StandardJsonQuery.QueryType.AUTH_DATA, 
															new LinkedHashMap<String, String>() {
																{
																put("login", textFieldLogin.getText());
																put("password", textFieldPass.getText());
																}
															});
		new Thread(() -> {
			try {
				CloudBoxClient.getInstance().start(jsonQuary);
	
			} catch (InterruptedException e) {
				logger.error("Server connection failed: " + e.getMessage(), e);
			}
		}).run();
	}

	/**
	 * отправить новые данные для аутентификации
	 */
	@FXML public void btnbtnSendAuthClickMeReaction() {}

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
		
		textFieldLogin.requestFocus();
		textFieldPassConfirm.clear();
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
		
		textFieldLogin.clear();
		textFieldLogin.requestFocus();
		textFieldPass.clear();
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
