package com.cloud.fx.controllers;

import java.awt.MouseInfo;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.cloud.CloudBoxClient;
import com.cloud.fx.Controller;
import com.cloud.fx.MessagesProcessor;
import com.cloud.fx.components.LabelWithInfo;
import com.cloud.fx.components.LabelWithInfo.FileType;
import com.cloud.logger.ClientConsoleLogAppender;
import com.cloud.utils.processors.FilesProcessor;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.json.JsonCreateDir;
import com.cloud.utils.queries.json.JsonDelete;
import com.cloud.utils.queries.json.JsonGetFile;
import com.cloud.utils.queries.json.JsonGetFilesList;
import com.cloud.utils.queries.json.JsonRename;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;

/**
 * Осуществляет управление основным экраном порльзовательского интерфейса
 * @author prozorova 10.10.2018
 */
public class MainSceneController extends Controller implements Initializable {
	
	private static final Logger logger = Logger.getLogger(MainSceneController.class);
	private static final MessagesProcessor MESSAGES_PROCESSOR = MessagesProcessor.getProcessor();
	
	private static final FilesProcessor FILES_PROCESSOR = new FilesProcessor();
	
	private static final String DEF_SERVER_DIR = "Server";
	
	private String currentDirClient;
	private String currentDirServer = File.separator;
	private String newDirServer;
    
	
	
    @FXML private TextField textField;

    @FXML private TextFlow textFlowConsole;
    @FXML private ScrollPane scrollPaneConsole;
    
    private Set<LabelWithInfo> filesOnClient;
    private Set<LabelWithInfo> filesOnServer;
    private Comparator<LabelWithInfo> comp;

	@FXML private BorderPane mainBorderPane;
	
	// CLIENT TABLEVIEW
	@FXML private Label lblClientPath;
	@FXML private TableView<LabelWithInfo> tableClient;
	@FXML private TableColumn<LabelWithInfo, Label> label;
	@FXML private TableColumn<LabelWithInfo, String> fileSize;
	@FXML private TableColumn<LabelWithInfo, String> dateModified;

	// SERVER TABLEVIEW
	@FXML private Label lblServerPath;
	@FXML private TableView<LabelWithInfo> tableServer;
	@FXML private TableColumn<LabelWithInfo, Label> labelServ;
	@FXML private TableColumn<LabelWithInfo, String> fileSizeServ;
	@FXML private TableColumn<LabelWithInfo, String> dateModifiedServ;
	
	// текущие элементы в таргете
	private LabelWithInfo currentTargetClient;
	private LabelWithInfo currentTargetServer;
	@FXML Button btnRename;
	
    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
    	// на всякий случай, чтобы инициализация прошла успешно даже при проблеме с логгером
    	try {
    		ClientConsoleLogAppender.setController(this);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	// устанавливаем тип и значение которое должно хранится в колонке
    	label.setCellValueFactory(cellData -> cellData.getValue().getLabelProperty()); 
    	fileSize.setCellValueFactory(cellData -> cellData.getValue().getSizeProperty());
    	dateModified.setCellValueFactory(cellData -> cellData.getValue().getDateModifiedProperty());
    	
    	labelServ.setCellValueFactory(cellData -> cellData.getValue().getLabelProperty()); 
    	fileSizeServ.setCellValueFactory(cellData -> cellData.getValue().getSizeProperty());
    	dateModifiedServ.setCellValueFactory(cellData -> cellData.getValue().getDateModifiedProperty());
    	
    	// переопределяем компаратор для сетов, чтобы каталоги всегда были в начале списка
		comp = new Comparator<LabelWithInfo>() {
			@Override
			public int compare(LabelWithInfo label1, LabelWithInfo label2) {
				if (label1.getFileType() == FileType.DIRECTORY && label2.getFileType() == FileType.FILE)
					return -1;
				else if (label1.getFileType() == FileType.FILE && label2.getFileType() == FileType.DIRECTORY)
					return 1;
				else
					return label1.getFileName().compareTo(label2.getFileName());
			}
		};
		
		// отображаем список файлов на сервере в своей корневой папке
		refresh(FilesSource.SERVER);
		// отображаем над таблицами текущие папки
		lblClientPath.getStyleClass().add("pathLabels");
		lblServerPath.getStyleClass().add("pathLabels");
		
	}
    
    /* ******* ACTION BUTTONS ******* */

	@FXML public void btnShareClickMeReaction() {}


	
	@FXML public void btnConnectClickMeReaction() {}
	
	
	/**
	 * создать новую папку
	 */
	@FXML public void btnNewFolderClickMeReaction() {
		ButtonType type = getConfirmation("New folder creation", 
                                          "Where do you want to create new folder?",
                                          "Server",
                                          "Client");
		String name = getAdditionalInformation("New folder creation", "Input directory name:", "directory name");
		String log;
		
		if (type != null && name != null && !name.equals("")) 
			switch (type.getText()) {

			case "Server":
				MESSAGES_PROCESSOR.sendTransference(new JsonCreateDir(currentDirServer, name));
				break;

			case "Client":
				try {
					log = FILES_PROCESSOR.createFolder(currentDirClient, name);
					changeFolderClient(Paths.get(currentDirClient));
					logger.debug(log);
					throwAlertMessage("INFO", log);
				} catch (Exception e) {
					logger.error("Folder creation failed: "+e.getMessage(), e);
					throwAlertMessage("ERROR", "Folder creation failed.");
				}
				break;
			}
		
	}


	@FXML public void btnContactsClickMeReaction() {}


	/**
	 * Удалить выбранный файл или папку
	 */
	@FXML public void btnDeleteClickMeReaction() {
		String filePath;
		String log;
			
		// если выбран файл на панели клиента
		if (currentTargetClient != null) {
			filePath = currentTargetClient.getFile().toPath().toString();
			
			if (getConfirmation("Delete File or Directory", "Are you sure want to remove "+filePath+"?") == ButtonType.CANCEL)
					return;
			
			try {
				log = FILES_PROCESSOR.deleteFile(filePath);
				throwAlertMessage("INFO", log);
				logger.debug(log);

				filesOnClient.remove(currentTargetClient);
				showFilesInDir(FilesSource.CLIENT);

				currentTargetClient = null;
				
			} catch (Exception e) {
				throwAlertMessage("ERROR", "Removing "+filePath+" failed.");
				logger.error("Removing "+filePath+" failed: "+e.getMessage(), e);
			}
		
		// если выбран файл на панели сервера
		} else if (currentTargetServer != null) {
			filePath = currentDirServer + currentTargetServer.getFileName();
			
			if (getConfirmation("Delete File or Directory", "Are you sure want to remove "+filePath+"?") == ButtonType.CANCEL)
				return;
		
			MESSAGES_PROCESSOR.sendTransference(new JsonDelete(filePath));
			
			currentTargetServer = null;
			
		} else {
			throwAlertMessage("ERROR", "Choose file or directory for removing.");
		}
	}


	@FXML public void btnExportClickMeReaction() {}


	@FXML public void btnImportClickMeReaction() {}


	/**
	 * перейти в свой корневой каталог
	 */
	@FXML public void btnHomeClickMeReaction() {
		
		this.newDirServer = File.separator;
		MESSAGES_PROCESSOR.sendTransference(new JsonGetFilesList(newDirServer));
	}
	
	/**
	 * переименовать файл или каталог
	 */
	@FXML public void btnRenameClickMeReaction() {
		Path filePath;
		String log;
		String newName;
		
		// если выбран файл на панели клиента
		if (currentTargetClient != null) {
			filePath = currentTargetClient.getFile().toPath();

			newName = getAdditionalInformation("Rename File or Directory", 
					                           "Input new name for "+filePath+":", 
					                           currentTargetClient.getFileName());
			if (newName == null)
				return;
			
			try {
				log = FILES_PROCESSOR.moveFile(filePath.toString(), filePath.getParent() + File.separator + newName);
				throwAlertMessage("INFO", log);
				logger.debug(log);

				changeFolderClient(filePath.getParent());
				currentTargetClient = null;

			} catch (Exception e) {
				throwAlertMessage("ERROR", "Renaming "+filePath+" failed.");
				logger.error("Renaming "+filePath+" failed: "+e.getMessage(), e);
			}

		// если выбран файл на панели сервера
		} else if (currentTargetServer != null) {
			filePath = Paths.get(currentDirServer + currentTargetServer.getFileName());

			newName = getAdditionalInformation("Rename File or Directory", 
					                           "Input new name for "+filePath+":", 
					                           currentTargetServer.getFileName());
			if (newName == null)
				return;

			MESSAGES_PROCESSOR.sendTransference(new JsonRename(filePath.toString(), newName));
			currentTargetServer = null;

		} else {
			throwAlertMessage("ERROR", "Choose file or directory for renaming.");
		}
		
	}


	@FXML public void btnLogOffClickMeReaction() {}
	
	/*
     * TODO переделать в консоль + получение консольных команд
     */
    @FXML public void sendMsgClickMeReaction() {
    	Text text = new Text(textField.getText() + '\n');
    	writeToConsole(text);
        textField.clear();
        textField.requestFocus();
    }

	/**
	 * Открыть для просмотра директорию для начала работы на клиенте
	 */
	@FXML public void btnNewClickMeReaction() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Open Resource Folder");
		File selectedDir = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());
		
		 if (selectedDir != null && selectedDir.isDirectory())
			 changeFolderClient(selectedDir.toPath());
	}


	@FXML public void btnPropertiesClickMeReaction() {}


	@FXML public void btnAddAllClickMeReaction() {}


	@FXML public void btnAddClickMeReaction() {
		if (currentTargetClient != null)
			copyFile(currentTargetClient, FilesSource.CLIENT, FilesSource.SERVER);
		currentTargetClient = null;
	}


	@FXML public void btnLoadClickMeReaction() {
		if (currentTargetServer != null)
			copyFile(currentTargetServer, FilesSource.SERVER, FilesSource.CLIENT);
		currentTargetServer = null;
	}


	@FXML public void btnLoadAllClickMeReaction() {}


	@FXML public void cloudExit() {
		System.exit(0);
	}
	
	
	
	/* ******* OTHER METHODS ******* */
	
	/**
	 * вывести окно с кнопками: OK и Cancel по умолчанию
	 * @param header заголовок
	 * @param msg текст сообщения
	 * @return выбор пользователя
	 */
	private ButtonType getConfirmation (String header, String msg, String...buttons)  {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(header);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		
		if (buttons.length != 0) {
			alert.getButtonTypes().clear();
			ButtonType[] types = new ButtonType[buttons.length];
			
			for (int i = 0; i < buttons.length; i++)
				types[i] = new ButtonType(buttons[i]);
			
			alert.getButtonTypes().addAll(types);
			
		}
		Optional<ButtonType> option = alert.showAndWait();
		
		return option.get();
	}
	
	/**
	 * запрос дополнительной информации у пользователя
	 * @param header заголовок
	 * @param msg текст сообщения
	 * @return полученная информация
	 */
	private String getAdditionalInformation(String header, String msg, String fileName) {
		
		TextInputDialog dialog = new TextInputDialog(fileName);
		dialog.setTitle(header);
		dialog.setContentText(msg);

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent())
			return result.get();
		else
			return null;
	}
	
	
	/**
	 * Собрать все файлы из папки в отсортированный список
	 * @param path путь к папке
	 * @return отсортированный список файлов с доп. информацией
	 * @throws IOException 
	 */
	private Set<LabelWithInfo> gatherFilesInDirClient(Path path) throws IOException {
		
		Set<LabelWithInfo> filesSet = new TreeSet<>(comp);
		
		Files.newDirectoryStream(path, filePath -> !filePath.equals(path))
	     .forEach(new Consumer<Path>() {

			@Override
			public void accept(Path filePath) {
				try {
					LabelWithInfo myLabel = new LabelWithInfo(filePath.toFile());

					// инициализация поведения и добавление в список
					initializeBehaviorLabel(myLabel, FilesSource.CLIENT);
					filesSet.add(myLabel);
					
				} catch (IOException e) {
					logger.error("Creation LabelWithInfo object for "+filePath.toFile()+" failed: " + e.getMessage(), e);
				}
			}
		});
		return filesSet;
	}
	
	/**
	 * Собрать список файлов, полученный от сервера, в отсортированный список LabelWithInfo
	 * @param set список файлов, полученный от сервера
	 * @return отсортированный список файлов с доп. информацией
	 */
	private Set<LabelWithInfo> gatherFilesInDirServer(Set<String> set) {
		
		Set<LabelWithInfo> filesSet = new TreeSet<>(comp);
		
		set.stream().forEach(new Consumer<String>() {

			@Override
			public void accept(String fileInfo) {
				
				LabelWithInfo myLabel = null;
				try {
					// парсим информацию о файле
					String[] fileAttribute = fileInfo.split(StandardJsonQuery.DIVIDER);
					String fileName = fileAttribute[0];
					long   fileSize = Long.parseLong(fileAttribute[1]);
					long   modMills = Long.parseLong(fileAttribute[2]);
					boolean   isDir = Boolean.parseBoolean(fileAttribute[3]);
					
					myLabel = new LabelWithInfo(fileName, fileSize, modMills, isDir);
				} catch (Exception e) {
					logger.error("Incorrect format of file information recieved from server: " + e.getMessage(), e);
				}

				if (myLabel != null) {
					// инициализация поведения и добавление в список
					initializeBehaviorLabel(myLabel, FilesSource.SERVER);
					filesSet.add(myLabel);
				} else
					logger.error("file "+fileInfo+" skipped");
			}
		});
		return filesSet;
	}
	
	@Override
	public void refresh(FilesSource source) {
		switch (source) {
		case CLIENT:
			changeFolderClient(Paths.get(currentDirClient));
			break;
		case SERVER:
			
			if (newDirServer != null) {
				currentDirServer = newDirServer;
				newDirServer = null;
			}
			
			lblServerPath.setText(DEF_SERVER_DIR+currentDirServer);
			Set<String> files = getFilesOnServer();
			if (files != null )
				this.filesOnServer = gatherFilesInDirServer(files);
			showFilesInDir(FilesSource.SERVER);
			
			break;
		}
		
	}
	
	/**
	 * Вспомогательный метод - отрисовывает файлы в папке
	 * @param source сервер или клиент
	 */
	private void showFilesInDir(FilesSource source) {
		ObservableList<LabelWithInfo> filesInDir = FXCollections.observableArrayList();
		// список файлов
		Set<LabelWithInfo> files;
		// где отрисовывать
		TableView<LabelWithInfo> tableView;
		
		// задать элемент перехода на уровень выше
		LabelWithInfo rootLabel = new LabelWithInfo();
		rootLabel.getLabel().setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				if (source == FilesSource.CLIENT) 
					changeFolderClient(Paths.get(currentDirClient).getParent());

				else {
					try {
						this.newDirServer = Paths.get(currentDirServer).getParent().toString();
						if (!newDirServer.endsWith(File.separator))
							newDirServer = newDirServer + File.separator;
						StandardJsonQuery json = new JsonGetFilesList(newDirServer);
						MESSAGES_PROCESSOR.sendTransference(json);
					} catch (NullPointerException e) {
						this.newDirServer = null;
						Controller.throwAlertMessage("ERROR", "It's your root directory!");
					}
				}
			}
		});
		
		if (source.equals(FilesSource.CLIENT)) {
			files = this.filesOnClient;
			tableView = this.tableClient;
		} else {
			files = this.filesOnServer;
			tableView = this.tableServer;
		}
		
		tableView.getItems().clear();
		
		filesInDir.add(rootLabel);
		if (files != null && !files.isEmpty())
			filesInDir.addAll(files);
		
		tableView.setItems(filesInDir);

	}
	
	/**
	 * сменить отображаемую папку на клиенте
	 * @param pathToFolder путь к папке
	 */
	private void changeFolderClient (Path pathToFolder) {
		if (pathToFolder == null)
			return;
		currentTargetClient = null;
		
		try {
	    	// собрать и отобразить все файлы в выбранной папке
	    	this.filesOnClient = gatherFilesInDirClient(pathToFolder);
	    	showFilesInDir(FilesSource.CLIENT);
	    	
	    	currentDirClient = pathToFolder.toString();
	    	lblClientPath.setText(currentDirClient);

	    } catch (IOException e) {
	    	logger.error("Opening "+pathToFolder+" directory is failed: " + e.getMessage(), e);
	    }
	}
	
	/**
	 * задаем поведение отображаемых элементов
	 * @param file элемент каталога
	 * @param filesSource клиент или сервер
	 */
	private void initializeBehaviorLabel(LabelWithInfo file, FilesSource filesSource) {
		Label label = file.getLabel();
		
		label.setOnDragDetected(event -> {
			Dragboard db = label.startDragAndDrop(TransferMode.COPY);
			ClipboardContent content = new ClipboardContent();
			ImageView iv = (ImageView) label.getGraphic();
			content.putImage(iv.getImage());
			db.setContent(content);
			event.consume();
		});

		label.setOnDragOver(event -> {
			if (event.getGestureSource() != label && event.getDragboard().hasImage()) {
				event.acceptTransferModes(TransferMode.COPY);
			}
			event.consume();
		});
		
		label.setOnDragDone(event -> {
			Dragboard db = event.getDragboard();
			
			double mouseX = MouseInfo.getPointerInfo().getLocation().getX();
			double mouseY = MouseInfo.getPointerInfo().getLocation().getY();
			
			// задаем направление перемещения файла
			FilesSource moveTo = (filesSource == FilesSource.CLIENT ? FilesSource.SERVER : FilesSource.CLIENT);
			
			TableView<LabelWithInfo> destination = (filesSource == FilesSource.CLIENT ? tableServer : tableClient);
			
			if (db.hasImage() && mouseX > destination.localToScreen(destination.getBoundsInLocal()).getMinX()
					          && mouseX < destination.localToScreen(destination.getBoundsInLocal()).getMaxX()
					          && mouseY > destination.localToScreen(destination.getBoundsInLocal()).getMinY()
					          && mouseY < destination.localToScreen(destination.getBoundsInLocal()).getMaxY() ){
				
					copyFile(file, filesSource, moveTo);
			}
			event.consume();
		});

		label.setOnMouseClicked(event -> {
			// если кликнули по элементу левой кнокой мыши
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				
				EventTarget eventTarget = event.getTarget();
				logger.debug("Clicked: " + eventTarget);
				
				if (filesSource == FilesSource.CLIENT) {
					currentTargetClient = file;
					currentTargetServer = null;
				}
				else {
					currentTargetServer = file;
					currentTargetClient = null;
				}
				
				// двойное нажатие на значок директории
				if (event.getClickCount() == 2 && file.getFileType() == FileType.DIRECTORY) {
					
					if (filesSource == FilesSource.CLIENT)
						changeFolderClient(file.getFile().toPath());
					else {
						this.newDirServer = currentDirServer+file.getFileName()+File.separator;
						MESSAGES_PROCESSOR.sendTransference(new JsonGetFilesList(newDirServer));
					}
				}
			}
		});
	}

	/**
	 * скопировать файл
	 * @param file файл
	 * @param fromfilesSource откуда копируем
	 * @param tofilesSource куда копируем
	 */
	private void copyFile(LabelWithInfo file, FilesSource fromfilesSource, FilesSource tofilesSource) {
		
		// отправить файл на сервер
		if (tofilesSource == FilesSource.SERVER)
			MESSAGES_PROCESSOR.sendTransference(file, currentDirServer);
		
		// запросить файл с сервера
		else {
			if (currentDirClient == null || currentDirClient.equals("")) {
				throwAlertMessage("ERROR", "No chosen directory for file to copy to.");
				return;
			}
			MESSAGES_PROCESSOR.sendTransference(new JsonGetFile(currentDirServer+file.getFileName()));
			addFile(currentDirServer+file.getFileName(), currentDirClient);
		}
	}
    
    /**
     * Вспомогательный метод для выведения текста в консоль
     * @param str текст для записи в консоль
     */
    public void writeToConsole(Node e) {
    	Platform.runLater(() -> {
    		textFlowConsole.getChildren().add(e);
    		scrollPaneConsole.vvalueProperty().bind(textFlowConsole.heightProperty());
		});
    }

	@Override
	protected void finalize() throws Throwable {
		try {
			CloudBoxClient.getInstance().disconnect();
		} finally {
			super.finalize();
		}
	}
}
