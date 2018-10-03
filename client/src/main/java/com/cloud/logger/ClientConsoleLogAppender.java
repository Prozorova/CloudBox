package com.cloud.logger;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

import com.cloud.fx.Controller;

import javafx.scene.text.Text;

public class ClientConsoleLogAppender extends RollingFileAppender {
	
	private static Controller controller;
	private static boolean switchOn = false;

	@Override
	protected void subAppend(LoggingEvent event) {
		super.subAppend(event);
		if (switchOn)
			controller.writeToConsole(new Text(event.getMessage().toString() + '\n'));
	}
	
	public static void setController(Controller contr) {
		controller = contr;
		switchOn = true;
	}
}
