package com.cloud.utils.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import com.cloud.utils.queries.TransferMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Протокол для передачи сообщения по сети: кодирует объект
 * TransferMessage в массив байтов
 * @author prozorova 10.10.2018
 */
public class TransferMessageEncoder extends MessageToByteEncoder<TransferMessage> {

	// для работы с json
	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void encode(ChannelHandlerContext ctx, TransferMessage msg, ByteBuf out) throws Exception {
		
		//TODO для тестирования - убрать
		mapper.writeValue(new FileOutputStream("111.json", false), msg.getJsonQuery());
		
		boolean isWithFile = false;    // передаем ли файл
		File file = msg.getFile();
		
		// будет ли передача файла
		if (file != null)
			isWithFile = true;

		byte[] jsonBytes = mapper.writeValueAsBytes(msg.getJsonQuery());
		
		// передаем размер json
		out.writeMedium(jsonBytes.length);
				
		// передаем служебную информацию в виде json
		out.writeBytes(jsonBytes);
		
		// будет ли передача файла
		out.writeBoolean(isWithFile);
		
		// передача файла
		if (isWithFile) {
			
			// TODO пока большие файлы не поддерживаются
			if (file.length() > Integer.MAX_VALUE)
				throw new Exception("Too big file");
			
			// передаем файл
			out.writeInt((int)file.length());
			out.writeBytes(Files.newInputStream(file.toPath()), (int)file.length());
		}
	}
}
