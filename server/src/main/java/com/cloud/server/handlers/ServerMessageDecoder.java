package com.cloud.server.handlers;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.cloud.server.AuthManager;
import com.cloud.server.DatabaseQueriesProcessor;
import com.cloud.utils.handlers.MessageDecodeHelper;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.StandardJsonQuery.QueryType;
import com.cloud.utils.queries.json.JsonSendFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Декодер сервера: распарсивает json, записывает файлы,
 * формирует объекты TransferMessage
 * @author prozorova 10.10.2018
 */
public class ServerMessageDecoder extends LengthFieldBasedFrameDecoder{
	
	public ServerMessageDecoder() {
        super(FileTransferHelper.BUFFER_LEN + 52, 0, 4, 0, 4);
    }

	private Map<String, JsonSendFile> files = new HashMap<>();
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
	
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        
        in.release();
        StandardJsonQuery out = null;
        
		int code = frame.readInt();
		
		// если получили json
		if (code == FileTransferHelper.CODE_JSON) {

			StandardJsonQuery jsonQuery = MessageDecodeHelper.decode(frame);
			
			
			if (jsonQuery.getQueryType() == QueryType.SEND_FILE) {              
				JsonSendFile json = (JsonSendFile)jsonQuery;
				                                                           // путь к папке клиента
				String path = DatabaseQueriesProcessor.getInstance().getPath(AuthManager.getUser(ctx.channel())) + 
						json.getFilePath() +                               // путь внутри папки
						File.separator +
						json.getFileName();                                // имя файла

				json.setFilePath(path);                    // меняем путь к файлу на текущий на сервере
				files.put(json.getFileCheckSum(), json);   // добавляем в список файлов на получение
				
				File file = new File(path);
				Files.deleteIfExists(file.toPath());
			} else
				out = jsonQuery;

			
			
			// если получили файл или его часть
		} else if (code == FileTransferHelper.CODE_FILE) {
			
			int length = frame.readInt();
			long position = frame.readLong();
			
			// TODO проверить владельца файла и контрольную сумму
			
			String fileID = frame.readCharSequence(32, Charset.defaultCharset()).toString();
			
			System.out.println("RECIEVING FILE: " + length + " *** " + fileID);
			JsonSendFile json = files.get(fileID);
			
			File file = new File(json.getFilePath());
			if (!file.exists())
				Files.createFile(file.toPath());
			
			try {
				MessageDecodeHelper.recieveFile(frame, length, position, file.toPath());
			} catch (Throwable e) {
				e.printStackTrace();
				
				json.setPartsAmount(-1);
				out = files.remove(fileID);

				throw e;
			}
			
			json.setPartsAmount(json.getPartsAmount()-1);
			if (json.getPartsAmount() == 0) {
				out = files.remove(fileID);
			}
			
		}
		return out;
	}
}
