package com.cloud.server.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.cloud.server.AuthManager;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.processors.FilesProcessor;
import com.cloud.utils.processors.StandardTransference;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonConfirm;
import com.cloud.utils.queries.json.JsonDelete;
import com.cloud.utils.queries.json.JsonGetFile;
import com.cloud.utils.queries.json.JsonGetFilesList;
import com.cloud.utils.queries.json.JsonSendFile;
import com.cloud.utils.queries.json.JsonSimpleMessage;

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
	
	private static final ExecutorService service = Executors.newFixedThreadPool(20);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, StandardJsonQuery msg) throws Exception {
		
		StandardJsonQuery.QueryType queryType = msg.getQueryType();
		
		StandardJsonQuery jsonAnswer = null;
		
		try {
			switch (queryType) {
			case REG_DATA:
			case AUTH_DATA:              // послать ответ на запрос аутентификации
				authManager.acceptAuth((JsonAuth)msg, filesProcessor, ctx.channel());
				break;
			case SEND_FILE:              // послать подтверждение получения файла
				JsonSendFile json = (JsonSendFile) msg;
				if (json.getPartsAmount() == 0)            // передача файла прошла успешно
					jsonAnswer = new JsonConfirm(filesProcessor
							.gatherFilesFromDir(Paths.get(json.getFilePath()).getParent().toString()));
				else
					jsonAnswer = new JsonConfirm();
				break;
			case CONFIRMATION: // TODO
				//				ctx.writeAndFlush(FileTransferHelper.prepareTransference(msg));
				break;
				
			case DELETE:     // удаление файла или папки
				JsonDelete jsonDel =  (JsonDelete) msg;
				try {
					Path path = Paths.get(jsonDel.getFilePath());
					String log = filesProcessor.deleteFile(path.toString());
					logger.debug(log);
					jsonAnswer = new JsonConfirm(filesProcessor.gatherFilesFromDir(path.getParent().toString()));
					
				} catch (Exception e) {
					logger.error("Removing "+jsonDel.getFilePath()+" failed: "+e.getMessage(), e);
					jsonAnswer = new JsonSimpleMessage("Removing "+jsonDel.getFilePath()+" failed.");
				}
				break;
				
			case GET_LIST:
				JsonGetFilesList jsonList =  (JsonGetFilesList) msg;
				// путь к директории в личном хранилище пользователя
				String dir = jsonList.getFilePath();
				try {
					// получаем список файлов в заданной папке
					jsonAnswer = new JsonConfirm(filesProcessor.gatherFilesFromDir(dir));
				} catch (Exception e) {
					logger.error("Gathering files from directory "+dir+" failed: "+e.getMessage(),e);
					jsonAnswer = new JsonSimpleMessage("Obtaining list of files in directory failed");
				} 
				break;
				
			case RENAME:
				break;
			case GET_FILE:
				JsonGetFile jsonGetFile = (JsonGetFile)msg;
				sendTransference(jsonGetFile.getFilePath(), jsonGetFile.getFilePathOld(), ctx);
				break;

			default:      // все ошибочные сообщения, которые не должны поступать на сервер
				throw new IllegalDataException(queryType);

			}
		} finally {
			if (jsonAnswer != null) {
				ctx.writeAndFlush(FileTransferHelper.prepareTransference(jsonAnswer));
				jsonAnswer = null;
			}
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
	
	
	/**
	 * 
	 * @param pathToFile
	 * @param path путь к файлу, как передал его клиент
	 * @param ctx
	 */
	private void sendTransference(String pathToFile, String path, ChannelHandlerContext ctx) {
		
    	File file;
    	long fileSize;
    	
		try {
			file = Paths.get(pathToFile).toFile();
			fileSize = (long) Files.getAttribute(file.toPath(), "basic:size");
		} catch (Exception e) {
			logger.error("File for transfer ("+pathToFile+") doesn't exists: "+e.getMessage(),e);
			
			try {
				ctx.writeAndFlush(FileTransferHelper
						.prepareTransference(new JsonSimpleMessage("Server error: file doesn't exists.")));
			} catch (IOException | IllegalDataException e1) {
				logger.error(e.getMessage(), e);
			}
			return;
		}
		
		Thread t = new Thread(() -> {
			JsonSendFile jsonQuery = null;
			
			try {
				jsonQuery = new JsonSendFile(file.getName(),
											fileSize,
											FileTransferHelper.get32Hex(file),
											path,     
											1);

				int parts = (int)(file.length()/FileTransferHelper.BUFFER_LEN) + 1;
				jsonQuery.setPartsAmount(parts);


				ctx.writeAndFlush(FileTransferHelper.prepareTransference(jsonQuery));

				for (int i = 0; i < parts; i++) {
					StandardTransference transference = FileTransferHelper.prepareTransference(file, i, jsonQuery.getFileCheckSum());
					ctx.writeAndFlush(transference);
				} 
			} catch (Exception e) {
				logger.error("File ("+pathToFile+") transference failed: " + e.getMessage(), e);
				
				try {
					ctx.writeAndFlush(FileTransferHelper
							.prepareTransference(new JsonSimpleMessage("Server error: file transference failed.")));
				} catch (IOException | IllegalDataException e1) {
					logger.error(e.getMessage(), e);
				}
			}
		});
		
    	t.setDaemon(true);
    	service.submit(t);
    }
	
	
}
