package com.cloud.handlers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.cloud.fx.Controller;
import com.cloud.utils.handlers.MessageDecodeHelper;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.queries.StandardJsonQuery;
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
	
	public ClientMessageDecoder() {
        super(FileTransferHelper.BUFFER_LEN + 52, 0, 4, 0, 4);
    }

	private MessageDecodeHelper decoder = new MessageDecodeHelper();
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        
        in.release();
        
        StandardJsonQuery json = decoder.decodeTransference(frame, MessageDecodeHelper.CLIENT_COM_SENDFILE);

        if (json instanceof JsonSendFile) {
        	JsonSendFile jsonFile = (JsonSendFile)json;
        	
        	if (jsonFile.getPartsAmount() > 0) {
        	
	        	String path = Controller.getFilePath(jsonFile.getFilePath()) +
	        			      File.separator +
	        			      jsonFile.getFileName();
	        	
	        	jsonFile.setFilePath(path);
	        	Files.deleteIfExists(Paths.get(path));
	        	
	        	json = null;
        	}
        }
        return json;
	}
}
