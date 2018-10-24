package com.cloud.fx.controllers;

import java.awt.MouseInfo;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.cloud.CloudBoxClient;
import com.cloud.fx.Controller;
import com.cloud.fx.MessagesProcessor;
import com.cloud.fx.components.LabelWithInfo;
import com.cloud.fx.components.LabelWithInfo.FileType;
import com.cloud.logger.ClientConsoleLogAppender;
import com.cloud.utils.processors.FileTransferHelper;
import com.cloud.utils.processors.StandardTransference;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.json.JsonSendFile;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

/**
 * Осуществляет управление основным экраном порльзовательского интерфейса
 * @author prozorova 10.10.2018
 */
public class MainSceneController extends Controller implements Initializable {
	
	private static final Logger logger = Logger.getLogger(MainSceneController.class);

	private static final String DEF_SERVER_DIR = "Server/";
	
	private String currentDirClient;
	private String currentDirServer = "";
    
	// для регулирования обработки - на сервере или на клиенте
	enum FilesSource {SERVER, CLIENT};
	
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
		
		refresh();
		lblServerPath.setText(DEF_SERVER_DIR);
		lblClientPath.getStyleClass().add("pathLabels");
		lblServerPath.getStyleClass().add("pathLabels");
		
	}

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
     * Вспомогательный метод для выведения текста в консоль
     * @param str текст для записи в консоль
     */
    public void writeToConsole(Node e) {
    	Platform.runLater(() -> {
    		textFlowConsole.getChildren().add(e);
    		scrollPaneConsole.vvalueProperty().bind(textFlowConsole.heightProperty());
		});
    }

    public void testSend(ActionEvent actionEvent) {
        try {
            Socket socket = new Socket("localhost", 8189);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//            MyFile mf = new MyFile();
//            oos.writeObject(mf);
            oos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	@FXML public void btnShareClickMeReaction() {}


	
	@FXML public void btnConnectClickMeReaction() throws IOException {
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
					initializeBehaviorLabel(myLabel, FilesSource.CLIENT);
					filesSet.add(myLabel);
				} else
					logger.error("file "+fileInfo+" skipped");
			}
		});
		return filesSet;
	}
	
	@Override
	public void refresh() {
		Set<String> files = getFilesOnServer();
		if (files != null && !files.isEmpty())
			this.filesOnServer = gatherFilesInDirServer(files);
		showFilesInDir(FilesSource.SERVER);
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

				else //TODO
					System.out.println("Double clicked");
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
				
				if (filesSource == FilesSource.CLIENT) 
					currentTargetClient = file;
				else 
					currentTargetServer = file;
				
				// двойное нажатие на значок директории
				if (event.getClickCount() == 2 && file.getFileType() == FileType.DIRECTORY) {
					
					if (filesSource == FilesSource.CLIENT) 
						changeFolderClient(file.getFile().toPath());
					else 
						System.out.println("Double clicked - SERVER");
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
		
		Set<LabelWithInfo> destSet = (tofilesSource == FilesSource.CLIENT ? filesOnClient : filesOnServer);

		JsonSendFile jsonQuery = null;
		try {
			jsonQuery = new JsonSendFile(file.getFileName(),
					                     file.getFileSizeBytes(),
					                     FileTransferHelper.get32Hex(file.getFile()),    // Check sum   ???
					                     currentDirServer,
					                     1);
		} catch (IOException e) {
			logger.debug("File transfer failed: " + e.getMessage(),e);
		}
		//			if (tofilesSource == FilesSource.CLIENT)
		//				filePath = currentDirClient;

		if (jsonQuery != null)
			MessagesProcessor.getProcessor().sendTransference(file.getFile(), jsonQuery);
		else {}
			// TODO

	}
	
	

	@FXML public void btnCopyClickMeReaction() {}


	@FXML public void btnContactsClickMeReaction() {}


	@FXML public void btnDeleteClickMeReaction() {}


	@FXML public void btnExportClickMeReaction() {}


	@FXML public void btnImportClickMeReaction() {}


	/**
	 * перейти в свой корневой каталог
	 */
	@FXML public void btnHomeClickMeReaction() {
		this.currentDirServer = DEF_SERVER_DIR;
		this.filesOnServer = gatherFilesInDirServer(this.getFilesOnServer());
		showFilesInDir(FilesSource.SERVER);
		lblServerPath.setText(DEF_SERVER_DIR);
	}


	@FXML public void btnLogOffClickMeReaction() {}


	/**
	 * Открыть для просмотра директорию для начала работы на клиенте
	 * сейчас используется для тестирования - показывает файлы в папках, 
	 * позволяет имитировать работу дропбокса: можно перекидывать файлы
	 * с клиента на сервер (осуществляется реальное клиент-серверное
	 * взаимодействие и копирование файла)
	 * @throws IOException
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


	@FXML public void btnLoadClickMeReaction() {}


	@FXML public void btnLoadAllClickMeReaction() {}


	@FXML public void cloudExit() {
		System.exit(0);
	}
	
	private static String filePath;


	public static String getFilePath() {
		return filePath;
		
	}

	@Override
	protected void finalize() throws Throwable {
		try {
//			CloudBoxClient.getInstance().disconnect();
		} finally {
			super.finalize();
		}
	}
	
}
