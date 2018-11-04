package com.cloud.utils.processors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import com.cloud.utils.exep.IncorrectPathException;
import com.cloud.utils.queries.StandardJsonQuery;

/**
 * Осуществляет работу с файлами
 * @author prozorova 16.10.2018
 */
public class FilesProcessor {
	
	private static final String div = StandardJsonQuery.DIVIDER;
	
	/**
	 * Собрать в сет информацию о файлах в папке
	 * @param path путь к папке
	 * @return список файлов
	 * @throws IOException
	 */
	public Set<String> gatherFilesFromDir(String pathDir) throws Exception {
		
		Path path = Paths.get(pathDir);
			
		if (!path.toFile().isDirectory())
			throw new IncorrectPathException("Files gathering from "+path+" failed");
		
		Set<String> set = new HashSet<>();
		List<IOException> exceptions = new ArrayList<>();
		
		// собираем все файлы/папки в коллекцию и убираем из нее текущую папку (в которой ищем)
		Files.newDirectoryStream(path, filePath -> !filePath.equals(path))
		     .forEach(new Consumer<Path>() {

			@Override
			public void accept(Path filePath) {
				File file = filePath.toFile();
				// записываем всю информацию о файле
					try {
						set.add(file.getName() + div + 
						        Files.getAttribute(file.toPath(), "basic:size") + div +
						        Files.getLastModifiedTime(file.toPath()).toMillis() + div +
						        file.isDirectory());
					} catch (IOException e) {
						exceptions.add(e);
					}
			}
		});
		
		// чтобы пробросить исключение на уровень выше, тут обрабатывать не хочу
		if (!exceptions.isEmpty()) 
			throw exceptions.get(0);
		
		return set;
	}
	
	/**
	 * удалить файл
	 * @param filePath путь к файлу
	 * @return информация для записи в лог
	 * @throws Exception
	 */
	public String deleteFile(String filePath) throws Exception {
		
		Path path;
		String log;
		
		try {
			path = Paths.get(filePath);
		} catch (Exception e) {
			throw new IncorrectPathException(filePath, e);
		}
		
		if (Files.isDirectory(path)) {
			FileUtils.deleteDirectory(path.toFile());
			log = "   Directory "+filePath+" and its contents deleted";
		} else {
			Files.delete(path);
			log = "   File "+filePath+" deleted";
		}
		return log;
	}
	
	/**
	 * переименовать или переместить файл
	 * @param currentFile путь к файлу для переименования или перемещению
	 * @param newFile новый путь к файлу
	 * @return информация для записи в лог
	 * @throws Exception
	 */
	public String moveFile(String currentFile, String newFile) throws Exception {
	
		String log;
		
		Path curPath = Paths.get(currentFile);
		Path newPath = Paths.get(newFile);
		
		if (curPath.getParent().equals(newPath.getParent())) {
			Files.move(curPath, newPath);
			log = "   "+curPath.getFileName()+" renamed to "+newPath.getFileName();
		} else {
			FileUtils.moveToDirectory(curPath.toFile(), newPath.getParent().toFile(), true);
			log = "   "+curPath.getFileName()+" moved to "+newPath.getParent();
		}
		
		return log;
	}
	
	/**
	 * создание папки в заданной
	 * @param sourceDir директория, где будет создана новая
	 * @param folderName имя новой папки
	 * @return нформация для записи в лог
	 * @throws Exception
	 */
	public String createFolder(String sourceDir, String folderName) throws Exception {
		String log; 
		
		Path newPath = Paths.get(sourceDir, folderName);
		
		if (newPath.toFile().exists())
			log = folderName+" folder already exists.";
		else {
			Files.createDirectory(Paths.get(sourceDir, folderName));
			log = folderName+" folder successfully created.";
		}
		
		return log;
	}
}
