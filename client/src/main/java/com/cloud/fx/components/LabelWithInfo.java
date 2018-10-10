package com.cloud.fx.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

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
	enum FileType {DIRECTORY, FILE};
	
	private Label label;
	private File file;
	private long fileSize;    // in bytes
	private FileTime dateModified;
	private FileType fileType;

	public LabelWithInfo (File file) throws IOException {
		
		this.file = file;
		this.fileSize = (long) Files.getAttribute(file.toPath(), "basic:size");
		this.dateModified = Files.getLastModifiedTime(file.toPath());
		this.fileType = Files.isDirectory(file.toPath()) ? FileType.DIRECTORY : FileType.FILE;
		
		Image icon;
		switch(fileType) {
			case FILE:
				icon = new Image(getClass().getResource("/icons/file.png").toExternalForm());
				break;
			default:   // DIRECTORY
				icon = new Image(getClass().getResource("/icons/directory.png").toExternalForm());
				break;
		}
		
		// оборачиваем иконку в ImageView и задаем параметры отрисовки
		ImageView imageView = new ImageView(icon);
		imageView.setFitHeight(20);
		imageView.setFitWidth(20);
		
		this.label = new Label(file.getName(), imageView);
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

	public String getFileSize() {
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

	public FileTime getDateModified() {
		return dateModified;
	}
	
	public ObjectProperty<FileTime> getDateModProperty() {
        return new SimpleObjectProperty<FileTime>(dateModified);
    }
	
	public FileType getFileType() {
		return fileType;
	}
}
