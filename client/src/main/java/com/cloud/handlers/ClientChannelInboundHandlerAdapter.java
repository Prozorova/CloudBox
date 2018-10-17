package com.cloud.handlers;

import com.cloud.fx.MessagesProcessor;
import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonConfirm;
import com.cloud.utils.queries.json.JsonResultAuth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * Хендлер клиента: обрабатывает входящие сообщения от сервера
 * @author prozorova 10.10.2018
 */
public class ClientChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
	
	// данные аутентификации
	private TransferMessage authMsg;
	
	private MessagesProcessor processor;
	
	public ClientChannelInboundHandlerAdapter(SocketChannel currentChannel, 
			                                  TransferMessage data,
			                                  MessagesProcessor processor) {
		super();
		this.authMsg = data;
		this.processor = processor;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(authMsg);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		TransferMessage read = (TransferMessage)msg;
    	switch (read.getJsonQuery().getQueryType()) {
			case AUTH_DATA:
				
				break;
			case AUTH_RESULT:
				JsonResultAuth jsonAuth = (JsonResultAuth)read.getJsonQuery();
				processor.login(jsonAuth.getAuthResult(), jsonAuth.getFiles(), jsonAuth.getReason());
				break;
			case SEND_FILE:
				break;
			case CONFIRMATION:
				JsonConfirm jsonConfirm = (JsonConfirm)read.getJsonQuery();
				if (jsonConfirm.getConfirmation())
					processor.refreshFilesOnServer(jsonConfirm.getFiles());
				else
					processor.showAlert("Error", "File transmitting failed");
			default:
				break;
    	}
	}
}
