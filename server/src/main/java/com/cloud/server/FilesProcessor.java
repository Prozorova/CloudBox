package com.cloud.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.cloud.utils.exep.IncorrectPathException;
import com.cloud.utils.queries.TransferMessage;

/**
 * Осуществляет работу с файлами на стороне сервера
 * @author prozorova 16.10.2018
 */
public class FilesProcessor {
	
	private static final Logger logger = Logger.getLogger(FilesProcessor.class);
	private final String div = TransferMessage.DIVIDER;
	
	/**
	 * Собрать в сет информацию о файлах в папке
	 * @param path путь к папке
	 * @return список файлов
	 * @throws IOException
	 */
	public Set<String> gatherFilesFromDir(Path path) throws IOException {
		if (path == null)
			throw new IncorrectPathException("Cannot gather files from null: ");
			
		if (!path.toFile().isDirectory())
			throw new IncorrectPathException("Files gathering from "+path+" failed: ");
		
		Set<String> set = new HashSet<>();
		
		Files.newDirectoryStream(path, filePath -> !filePath.equals(path))
		     .forEach(new Consumer<Path>() {

			@Override
			public void accept(Path filePath) {
				File file = filePath.toFile();
				try {
					set.add(file.getName() + div + 
					        Files.getAttribute(file.toPath(), "basic:size") + div +
					        Files.getLastModifiedTime(file.toPath()).toMillis() + div +
					        file.isDirectory());
					
				} catch (IOException e) {
					logger.error("Obtaining file "+file.getName()+" information failed: " + e.getMessage(), e);
				}
			}
		});
		return set;
	}

}
