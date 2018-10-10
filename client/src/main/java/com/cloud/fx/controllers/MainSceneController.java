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
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.cloud.MessagesProcessor;
import com.cloud.fx.Controller;
import com.cloud.fx.components.LabelWithInfo;
import com.cloud.logger.ClientConsoleLogAppender;
import com.cloud.utils.queries.StandardJsonQuery;
import com.cloud.utils.queries.TransferMessage;
import com.cloud.utils.queries.json.JsonSendFile;

import io.netty.channel.SelectStrategy;
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
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TableColumn;

/**
 * Осуществляет управление основным экраном порльзовательского интерфейса
 * @author prozorova 10.10.2018
 */
public class MainSceneController extends Controller implements Initializable {
	
	private static final Logger logger = Logger.getLogger(MainSceneController.class);

    public static final String DIR_FOR_TESTING_CLIENT = "/Users/prozorova/Documents/icons/111";
    private static final String DIR_FOR_TESTING_SERVER = "/Users/prozorova/Documents/icons/222";
    
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
	@FXML private TableView<LabelWithInfo> tableClient;
	@FXML private TableColumn<LabelWithInfo, Label> label;
	@FXML private TableColumn<LabelWithInfo, String> fileSize;
	@FXML private TableColumn<LabelWithInfo, FileTime> dateModified;

	// SERVER TABLEVIEW
	@FXML private TableView<LabelWithInfo> tableServer;
	@FXML private TableColumn<LabelWithInfo, Label> labelServ;
	@FXML private TableColumn<LabelWithInfo, String> fileSizeServ;
	@FXML private TableColumn<LabelWithInfo, FileTime> dateModifiedServ;
	
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
    	dateModified.setCellValueFactory(cellData -> cellData.getValue().getDateModProperty());
    	
    	labelServ.setCellValueFactory(cellData -> cellData.getValue().getLabelProperty()); 
    	fileSizeServ.setCellValueFactory(cellData -> cellData.getValue().getSizeProperty());
    	dateModifiedServ.setCellValueFactory(cellData -> cellData.getValue().getDateModProperty());
    	
    	// переопределяем компаратор, чтобы каталоги всегда были в начале списка
		comp = new Comparator<LabelWithInfo>() {
			@Override
			public int compare(LabelWithInfo label1, LabelWithInfo label2) {
				if (label1.getFile().isDirectory() && !label2.getFile().isDirectory())
					return -1;
				else if (label1.getFile().isDirectory() && !label2.getFile().isDirectory())
					return 1;
				else
					return label1.getFile().getName().compareTo(label2.getFile().getName());
			}
		};
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


	/**
	 * сейчас используется для тестирования - показывает файлы в папках, 
	 * позволяет имитировать работу дропбокса: можно перекидывать файлы
	 * с клиента на сервер (осуществляется реальное клиент-серверное
	 * взаимодействие и копирование файла)
	 * @throws IOException
	 */
	@FXML public void btnConnectClickMeReaction() throws IOException {
		
		this.filesOnClient = gatherFilesFromDir(Paths.get(DIR_FOR_TESTING_CLIENT), FilesSource.CLIENT);
		this.filesOnServer = gatherFilesFromDir(Paths.get(DIR_FOR_TESTING_SERVER), FilesSource.SERVER);
   	
		showFilesInDir(FilesSource.CLIENT);
		showFilesInDir(FilesSource.SERVER);
	}
	
	/**
	 * Собрать все файлы из папки в отсортированный список
	 * @param path путь к папке
	 * @return отсортированный список файлов с типами
	 */
	private Set<LabelWithInfo> gatherFilesFromDir(Path path, FilesSource filesSource) throws IOException {
		
		Iterator<Path> iterator = Files.newDirectoryStream(path).iterator();
		
		Set<LabelWithInfo> filesSet = new TreeSet<LabelWithInfo>(comp);
		logger.debug("List of files in directory " + path + ":");
		
		while (iterator.hasNext()) {
			File file = iterator.next().toFile();
			
			// убираем из списка папку, в которой проверяем файлы
			if (path.equals(file.toPath()))
				continue;

			logger.debug("   - " + file);
			
			LabelWithInfo myLabel = new LabelWithInfo(file);
			
			// инициализация поведения и добавление в список
			initializeDragAndDropLabel(myLabel.getLabel());
			initializeDragAndDropDone(myLabel, filesSource);
			filesSet.add(myLabel);
		}
		return filesSet;
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
		
		if (source.equals(FilesSource.CLIENT)) {
			files = this.filesOnClient;
			tableView = this.tableClient;
		} else {
			files = this.filesOnServer;
			tableView = this.tableServer;
		}
		
		tableView.getItems().clear();
		
		filesInDir.addAll(files);
		
		tableView.setItems(filesInDir);

	}
	
	/**
	 * задаем параметры отображаемых элементов каталога
	 * @param label элемент каталога
	 */
	private void initializeDragAndDropLabel(Label label) {

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
	}
	
	private void initializeDragAndDropDone(LabelWithInfo file, FilesSource filesSource) {
		file.getLabel().setOnDragDone(event -> {
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
		

		file.getLabel().setOnMouseClicked(event -> {
			EventTarget eventTarget = event.getTarget();
			logger.debug("Clicked: " + eventTarget);
			if (filesSource == FilesSource.CLIENT) 
				currentTargetClient = file;
			else 
				currentTargetServer = file;
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

		try {
			StandardJsonQuery jsonQuery = new JsonSendFile(file.getFile().getName(),
					                                       file.getFileSizeBytes(),
					                                       "***",    // Check sum   ???
					                                       "");      // TODO path
			if (tofilesSource == FilesSource.CLIENT)
				// TODO зашить реальный путь
				filePath = DIR_FOR_TESTING_CLIENT;
			
			MessagesProcessor.getProcessor().sendData(new TransferMessage(jsonQuery).addFile(file.getFile()));
			
			destSet.add(file.copyFile());
			
			// перерисовываем файлы - актуально только в случае успешного копирования
			showFilesInDir(tofilesSource);
		} catch (IOException e) {
			logger.error("Copying file is failed: " + e.getMessage(), e);
		}
	}

	@FXML public void btnCopyClickMeReaction() {}


	@FXML public void btnContactsClickMeReaction() {}


	@FXML public void btnDeleteClickMeReaction() {}


	@FXML public void btnExportClickMeReaction() {}


	@FXML public void btnImportClickMeReaction() {}


	@FXML public void btnHomeClickMeReaction() {}


	@FXML public void btnLogOffClickMeReaction() {}


	@FXML public void btnNewClickMeReaction() {}


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
}
