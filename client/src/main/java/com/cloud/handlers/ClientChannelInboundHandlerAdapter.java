package com.cloud.handlers;

import com.cloud.MessagesProcessor;
import com.cloud.utils.queries.TransferMessage;
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
			                                  TransferMessage authMsg,
			                                  MessagesProcessor processor) {
		super();
		this.authMsg = authMsg;
		this.processor = processor;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(authMsg);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ctx.flush();
		TransferMessage read = (TransferMessage)msg;
    	switch (read.getJsonQuery().getQueryType()) {
			case AUTH_DATA:
				
				break;
			case AUTH_RESULT:
				JsonResultAuth json = (JsonResultAuth)read.getJsonQuery();
				processor.login(json.getAuthResult());
				break;
			case SEND_FILE:
				break;
			default:
				break;
    	}
    	
    	
	}
}
