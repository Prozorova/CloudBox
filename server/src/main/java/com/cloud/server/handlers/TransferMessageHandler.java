package com.cloud.server.handlers;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.cloud.server.AuthManager;
import com.cloud.server.FilesProcessor;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonConfirm;
import com.cloud.utils.queries.json.JsonSendFile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Хендлер сервера: обрабатывает входящие сообщения в соответствии с их спецификой
 * @author prozorova 10.10.2018
 */
public class TransferMessageHandler extends SimpleChannelInboundHandler<TransferMessage> {
	
	private AuthManager       authManager = new AuthManager();
	private FilesProcessor filesProcessor = new FilesProcessor();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TransferMessage msg) throws Exception {

		StandardJsonQuery.QueryType queryType = msg.getJsonQuery().getQueryType();
		
		switch (queryType) {
			case REG_DATA:
			case AUTH_DATA:     // послать ответ на запрос аутентификации
				authManager.acceptAuth((JsonAuth)msg.getJsonQuery(), filesProcessor, ctx.channel());
				break;
			case AUTH_RESULT:   // ошибочное сообщение - не может поступить на сервер
				break;
			case SEND_FILE:     // получен файл от пользователя, отправляем подтверждение
				Path path = Paths.get(((JsonSendFile)msg.getJsonQuery()).getFilePath()).getParent();
				StandardJsonQuery json = new JsonConfirm(filesProcessor.gatherFilesFromDir(path));
				ctx.writeAndFlush(new TransferMessage(json)); 
				break;
			
				
			default:
				throw new IllegalDataException(queryType);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelUnregistered(ctx);
	}

	//TODO доработать
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		authManager.removeFromMap(ctx.channel());
	}
	
	
}
