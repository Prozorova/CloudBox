package com.cloud.handlers;

import com.cloud.fx.Controller;
import com.cloud.fx.MessagesProcessor;
import com.cloud.fx.SceneManager.Scenes;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.json.JsonConfirm;
import com.cloud.utils.queries.json.JsonResultAuth;
import com.cloud.utils.queries.json.JsonSendFile;
import com.cloud.utils.queries.json.JsonSimpleMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Хендлер клиента: обрабатывает входящие сообщения от сервера
 * @author prozorova 10.10.2018
 */
public class ClientChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
	
	private MessagesProcessor processor = MessagesProcessor.getProcessor();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.writeAndFlush(processor.getAuthData());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		StandardJsonQuery json = (StandardJsonQuery)msg;
    	switch (json.getQueryType()) {
			case AUTH_RESULT:
				JsonResultAuth jsonAuth = (JsonResultAuth)json;
				processor.login(jsonAuth.getAuthResult(), jsonAuth.getFiles(), jsonAuth.getReason());
				break;
			case SEND_FILE:
				JsonSendFile jsonSend = (JsonSendFile) msg;
				if (jsonSend.getPartsAmount() == 0)            // передача файла прошла успешно
					processor.refreshFilesOnClient();
				else
					Controller.throwAlertMessage("ERROR", "File transferring failed.");

				break;
			case CONFIRMATION:
				JsonConfirm jsonConfirm = (JsonConfirm)json;
				if (jsonConfirm.getConfirmation())
					processor.refreshFilesOnServer(jsonConfirm.getFiles());
				else
					Controller.throwAlertMessage("Error", "File transmitting failed");
				break;
			case MESSAGE:
				JsonSimpleMessage jsonMsg = (JsonSimpleMessage)json;
				Controller.throwAlertMessage("Alert", jsonMsg.getMessage());
				Controller.getSceneManager().changeScene(Scenes.AUTH);
				break;
			
				
			default:       // AUTH_DATA, REG_DATA
				break;
    	}
	}
}
