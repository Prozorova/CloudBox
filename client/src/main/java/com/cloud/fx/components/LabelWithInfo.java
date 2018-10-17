package com.cloud.fx.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.cloud.fx.Controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Хранит и возвращает всю информацию о файле
 * @author prozorova
 */
public class LabelWithInfo {
	
	// TODO добавить типы файлов (напр., в зависимости от расширения файла)
	public enum FileType {DIRECTORY, FILE};
	
	private final DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private static final Image FILE_ICON = new Image(Controller.class.getResource("/icons/file.png").toExternalForm());
	private static final Image DIR_ICON  = new Image(Controller.class.getResource("/icons/directory.png").toExternalForm());
	
	private Label label;
	
	private File file;        // используется, если это файл на клиенте
	private String fileName;  // если файл на сервере
	
	private long fileSize;    // in bytes
	private String dateModified;
	private FileType fileType;

	/**
	 * конструктор для создания элемента перехода на один уровень вверх
	 */
	public LabelWithInfo () {
		this.label = new Label(" . . .                ");
		this.dateModified = "";
	}
	
	/**
	 * конструктор для файлов на клиенте
	 * @param file файл
	 * @throws IOException
	 */
	public LabelWithInfo (File file) throws IOException {
		
		this.file = file;
		this.fileName = file.getName();
		this.fileSize = (long) Files.getAttribute(file.toPath(), "basic:size");
		this.dateModified = formatter.format(Files.getLastModifiedTime(file.toPath()).toMillis());
		this.fileType = Files.isDirectory(file.toPath()) ? FileType.DIRECTORY : FileType.FILE;
		
		createLabel(file.getName());
	}
	
	/**
	 * конструктор для файлов, находящихся на сервере
	 * @param fileName имя файла
	 * @param fileSize размер
	 * @param dateModified дата изменения
	 * @param isDir является ли директорией
	 */
	public LabelWithInfo (String fileName, long fileSize, long dateModified, boolean isDir) {
		
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.dateModified = formatter.format(dateModified);
		this.fileType = isDir ? FileType.DIRECTORY : FileType.FILE;
		
		createLabel(fileName);
	}
	
	/**
	 * Вспомогательный метод для создания объекта Label
	 * @param fileName имя файла
	 */
	private void createLabel(String fileName) {
		Image icon;
		switch(this.fileType) {
			case FILE:
				icon = FILE_ICON;
				break;
			default:   // DIRECTORY
				icon = DIR_ICON;
				break;
		}
		
		// оборачиваем иконку в ImageView и задаем параметры отрисовки
		ImageView imageView = new ImageView(icon);
		imageView.setFitHeight(20);
		imageView.setFitWidth(20);
		
		this.label = new Label(fileName, imageView);
	}
	
	
    public LabelWithInfo copyFile() throws IOException {
    	LabelWithInfo copy = new LabelWithInfo(this.file);
        return copy;
    }

	public Label getLabel() {
		return label;
	}
	
	public ObjectProperty<Label> getLabelProperty() {
        return new SimpleObjectProperty<Label>(label);
    }

	public File getFile() {
		return file;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getFileSize() {
		if (fileName == null)
			return "";
		if (fileSize < 1024)
			return fileSize + "b";
		else if (fileSize < 1024 * 1024)
			return fileSize / 1024 + "Kb";
		else 
			return fileSize / (1024 * 1024) + "Mb";
	}
	
	public Long getFileSizeBytes() {
		return fileSize;
	}
	
	public StringProperty getSizeProperty() {
		return new SimpleStringProperty(getFileSize());
    }

	public String getDateModified() {
		return dateModified;
	}
	
	public StringProperty getDateModifiedProperty() {
		return new SimpleStringProperty(dateModified);
    }
	
	public FileType getFileType() {
		return fileType;
	}
}
