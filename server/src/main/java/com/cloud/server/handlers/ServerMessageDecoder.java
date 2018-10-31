package com.cloud.server.handlers;

import java.nio.file.Path;

import com.cloud.server.AuthManager;
import com.cloud.utils.handlers.MessageDecodeHelper;
import com.cloud.utils.processors.FileTransferHelper;

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
	
	private MessageDecodeHelper decoder = new MessageDecodeHelper();
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
	
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        
        in.release();

        Path path = AuthManager.getUserFolder(ctx.channel());
        if (path != null)
        	return decoder.decodeTransference(frame, AuthManager.getUserFolder(ctx.channel()).toString());
        else
        	return decoder.decodeTransference(frame, null);
		
	}
}
