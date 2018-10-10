package com.cloud.server.handlers;

import com.cloud.server.AuthManager;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonConfirm;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Хендлер сервера: обрабатывает входящие сообщения в соответствии с их спецификой
 * @author prozorova 10.10.2018
 */
public class TransferMessageHandler extends SimpleChannelInboundHandler<TransferMessage> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TransferMessage msg) throws Exception {

		StandardJsonQuery.QueryType queryType = msg.getJsonQuery().getQueryType();
		
		switch (queryType) {
			case AUTH_DATA:     // послать ответ на запрос аутентификации
				AuthManager.getAuthManagerInstance().acceptAuth((JsonAuth)msg.getJsonQuery(), ctx.channel());
				break;
			case AUTH_RESULT:   // ошибочное сообщение - не может поступить на сервер
				break;
			case SEND_FILE:     // получен файл от пользователя, отправляем подтверждение
				StandardJsonQuery json = new JsonConfirm("OK");
				ctx.writeAndFlush(new TransferMessage(json)).addListener(ChannelFutureListener.CLOSE);
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
}
