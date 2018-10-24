package com.cloud.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.cloud.fx.controllers.MainSceneController;
import com.cloud.utils.handlers.MessageDecodeHelper;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.StandardJsonQuery.QueryType;
import com.cloud.utils.queries.json.JsonSendFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Декодер клиента: распарсивает json, записывает файлы, формирует объекты
 * TransferMessage
 * 
 * @author prozorova 10.10.2018
 */
public class ClientMessageDecoder extends LengthFieldBasedFrameDecoder {

	private String path;
	
	public ClientMessageDecoder() {
        super(FileTransferHelper.BUFFER_LEN + 52, 0, 4, 0, 4);
    }

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        
        StandardJsonQuery out = null;
        
		int code = frame.readInt();

		// если получили json
		if (code == FileTransferHelper.CODE_JSON) {
			StandardJsonQuery jsonQuery = null;

			jsonQuery = MessageDecodeHelper.decode(frame);
			if (jsonQuery.getQueryType() == QueryType.SEND_FILE) {
				path = MainSceneController.getFilePath() +              // путь к папке, куда сохранить файл
					   File.separator +
					   ((JsonSendFile) jsonQuery).getFileName();        // имя файла

				((JsonSendFile) jsonQuery).setFilePath(path);           // меняем путь к файлу

				File file = new File(path);
				Files.deleteIfExists(file.toPath());
			}

			out = jsonQuery;

			// если получили файл или его часть
		} else if (code == FileTransferHelper.CODE_FILE) {

			int length = frame.readInt();
			long position = frame.readLong();

			// TODO проверить владельца файла и контрольную сумму

			byte[] fileID = new byte[16];
			frame.readBytes(fileID);

			MessageDecodeHelper.recieveFile(frame, length, position, Files.createFile(Paths.get(path)));

		}
		return out;
	}
}
