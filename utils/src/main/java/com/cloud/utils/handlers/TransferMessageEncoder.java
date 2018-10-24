package com.cloud.utils.handlers;

import java.nio.charset.Charset;

import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.processors.StandardTransference;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Протокол для передачи сообщения по сети: кодирует объект
 * TransferMessage в массив байтов
 * @author prozorova 10.10.2018
 */

public class TransferMessageEncoder extends MessageToByteEncoder<StandardTransference> {

	@Override
	protected void encode(ChannelHandlerContext ctx, StandardTransference msg, ByteBuf out) throws Exception {
	
		int code = msg.getTransferMessageCode();
		
		// записываем размер "посылки"
		int length = (code == FileTransferHelper.CODE_JSON) ? msg.getLength() + 4 + 4:
			                                                  msg.getLength() + 4 + 4 + 8 + 32;
		out.writeInt(length);
		
		// записываем код и размер содержимого
		out.writeInt(code);
		out.writeInt(msg.getLength());
		
		// если передаем файл или его часть
		if (code == FileTransferHelper.CODE_FILE) {
			System.out.println("SENDING FILE: " + msg.getLength() + " *** " + msg.getFileID());
			out.writeLong(msg.getStartPosition());
			
			out.writeCharSequence(msg.getFileID(), Charset.defaultCharset());
		}
		
		// передаем сами данные
		out.writeBytes(msg.getData());
		
	}
	
}
