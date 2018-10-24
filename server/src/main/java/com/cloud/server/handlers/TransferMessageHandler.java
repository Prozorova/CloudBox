package com.cloud.server.handlers;

import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.cloud.server.AuthManager;
import com.cloud.server.FilesProcessor;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonConfirm;
import com.cloud.utils.queries.json.JsonSendFile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Хендлер сервера: обрабатывает входящие сообщения в соответствии с их спецификой
 * @author prozorova 10.10.2018
 */
public class TransferMessageHandler extends SimpleChannelInboundHandler<StandardJsonQuery> {
	
	private AuthManager       authManager = new AuthManager();
	private FilesProcessor filesProcessor = new FilesProcessor();
	
	private static final Logger logger = Logger.getLogger(TransferMessageHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, StandardJsonQuery msg) throws Exception {
		
		StandardJsonQuery.QueryType queryType = msg.getQueryType();
		
		switch (queryType) {
			case REG_DATA:
			case AUTH_DATA:     // послать ответ на запрос аутентификации
				authManager.acceptAuth((JsonAuth)msg, filesProcessor, ctx.channel());
				break;
			case SEND_FILE:     // послать подтверждение получения файла
				JsonSendFile json = (JsonSendFile) msg;
				StandardJsonQuery jsonQuery;
				
				if (json.getPartsAmount() == 0)        // передача файла прошла успешно
					jsonQuery = new JsonConfirm(filesProcessor
						.gatherFilesFromDir(Paths.get(json.getFilePath()).getParent()));
				else
					jsonQuery = new JsonConfirm();
				
				ctx.writeAndFlush(FileTransferHelper.prepareTransference(jsonQuery));
				break;
			case CONFIRMATION: // TODO
//				ctx.writeAndFlush(FileTransferHelper.prepareTransference(msg));
				break;
				
			default:      // все ошибочные сообщения, которые не должны поступать на сервер
				throw new IllegalDataException(queryType);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
		authManager.removeFromMap(ctx.channel());
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		authManager.removeFromMap(ctx.channel());
		System.out.println("disconnected");
	}
	
	
}
