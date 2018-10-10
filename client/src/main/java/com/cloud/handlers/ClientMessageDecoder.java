package com.cloud.handlers;

import java.io.File;
import java.util.List;

import com.cloud.fx.controllers.MainSceneController;
import com.cloud.utils.handlers.TransferMessageDecoder;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonSendFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Декодер клиента: распарсивает json, записывает файлы,
 * формирует объекты TransferMessage
 * @author prozorova 10.10.2018
 */
public class ClientMessageDecoder extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		StandardJsonQuery jsonQuery = TransferMessageDecoder.decode(in);
		
		File file = null;
		
		if (in.readBoolean()) {
			int fileLength = in.readInt();
			String path = MainSceneController.getFilePath() +           // путь к папке, куда сохранить файл
					      File.separator +
					      ((JsonSendFile)jsonQuery).getFileName();      // имя файла
			ctx.flush();
			file = TransferMessageDecoder.recieveFile(in, fileLength, path);
		}
		
		out.add(new TransferMessage(jsonQuery).addFile(file));
	}
}
