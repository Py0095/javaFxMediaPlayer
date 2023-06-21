package com.finals2.mediaplayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX App
 */
public class App extends Application {

    private Stage stage;
    private Scene scene;
    private BorderPane rootLayout;

    private MediaPlayer player;
    private MediaView mediaView;
    private MediaModel currentMedia;

    private FileChooser fileChooser;
    private ObservableList<MediaModel> playlist;   
    private ObservableList<MediaModel> alreadyPlay;

    private TableView<MediaModel> tableView;

    private Button pauseButton;
    private Button stopButton;
    private Button playButton;
    private Button muteButton;
    private Button unmuteButton;
    private boolean endOfMedia;

    private Slider sliderVolume;
    private Slider progressBar;

    private VBox bottomLayout;

    private boolean singleLoop;
    private boolean globalLoop;
    private boolean shuffleMode;

    @Override
    public void start(Stage primatyStage) throws IOException {
        stage = primatyStage;
        stage.setTitle("Media Player");
        stage.getIcons().add(getImage("icon.png").getImage());

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(Constants.getExtensionFilter());

        playlist = FXCollections.observableArrayList();
        alreadyPlay = FXCollections.observableArrayList();

        // Layout
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: black");   
        rootLayout.setCenter(getImage("icon.png"));

        // Global
        createTopMenuBar();
        createBottomActionBar();
        creationTableView();

        // Scene
        scene = new Scene(rootLayout, 640, 480);
        
        scene.widthProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    updateViewsSize();
                });

        scene.heightProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    updateViewsSize();
                });
        updateViewsSize();

        stage.setScene(scene);
        stage.show();
    }

    public void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.show();
    }

    public void log(Object message) {
        System.out.println(message);
    }

    public void updateViewsSize() {
        if (player != null) {
            mediaView.setFitWidth(scene.getWidth());
            mediaView.setFitHeight(scene.getHeight() - bottomLayout.getHeight() - 100);
        }
        tableView.setPrefWidth(scene.getWidth());
        tableView.setPrefHeight(scene.getHeight() - bottomLayout.getHeight());
    }

    public ImageView getImage(String image) {
        Image img = new Image(getClass().getResourceAsStream(image));
        return new ImageView(img);
    }

    public void playShuffle(){
        if (playlist.size() > 0) {
            int index = -1;

            if(!globalLoop){
                ObservableList<MediaModel> notYetPlay = FXCollections.observableArrayList();
                notYetPlay = playlist.filtered((media) -> {
                    return !alreadyPlay.contains(media);
                });

                index = (int) (Math.random() * notYetPlay.size());
            } else {
                index = (int) (Math.random() * playlist.size());

                if(index == playlist.indexOf(currentMedia)){
                    index = (int) (Math.random() * playlist.size());
                }
            }

            MediaModel mediaModel = playlist.get(index);
            initPlayer(mediaModel);
        }
    }

    public void playNext(){
        if (playlist.size() > 0) {
            int index = playlist.indexOf(currentMedia);
            if (index < playlist.size() - 1) {
                MediaModel mediaModel = playlist.get(index + 1);
                initPlayer(mediaModel);
            }else {
                if (globalLoop) {
                    MediaModel mediaModel = playlist.get(0);
                    initPlayer(mediaModel);
                }
            }
        }
    }

    public void playPrevious(){
        if (playlist.size() > 0) {
            int index = playlist.indexOf(currentMedia);
            if (index > 0) {
                MediaModel mediaModel = playlist.get(index - 1);
                initPlayer(mediaModel);
            }
        }
    }

    private void initPlayer(MediaModel mediaModel) {
        currentMedia = mediaModel;
        alreadyPlay.add(currentMedia);
        stage.setTitle("Media Player - " + mediaModel.getFileName());
        tableView.getSelectionModel().select(mediaModel);

        Media media = new Media(mediaModel.getPath());

        if (player != null) {
            player.dispose();
        }

        player = new MediaPlayer(media);
        sliderVolume.setValue(50);

        player.setAutoPlay(true);
        endOfMedia = false;

        player.setOnReady(() -> {
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
        });

        player.setOnPlaying(() -> {
            playButton.setDisable(true);
            pauseButton.setDisable(false);
            stopButton.setDisable(false);
        });

        player.setOnPaused(() -> {
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(false);
        });

        player.setOnStopped(() -> {
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
        });

        player.setOnEndOfMedia(() -> {
            endOfMedia = true;
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            progressBar.setValue(0);
        
            if (singleLoop) {
                player.seek(Duration.ZERO);
                player.play();
            } else if (globalLoop) {
                playNext();
            } else if (shuffleMode) {
                playShuffle();
            } else {
                playNext();
            }
        });

        player.currentTimeProperty()
                .addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
                    double progress = newValue.toSeconds() / player.getTotalDuration().toSeconds();
                    progressBar.setValue(progress * 100);
                });

        mediaView = new MediaView(player);
        updateViewsSize();
        
        if(mediaModel.getFileName().endsWith(".mp3") || mediaModel.getFileName().endsWith(".m4a")){
            rootLayout.setCenter(getImage("icon.png"));
        } else {
            rootLayout.setCenter(mediaView);
        } 
    }

    private void openFile() {
        fileChooser.setTitle("Open File");

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                
                MediaModel model  = new MediaModel(playlist.size() + 1, file.getName());
                model.setPath(file.toURI().toASCIIString());
                model.setSize(file.getTotalSpace() / 100 / 1024 / 1024 / 1024);

                Media media = new Media(file.toURI().toASCIIString());
                model.setDuration(media.getDuration().toString());

                playlist.add(model);

                initPlayer(model);
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        }
    }

    private void getFiles() {
        fileChooser.setTitle("Open Files");

        List<File> listFiles = fileChooser.showOpenMultipleDialog(stage);

        if (listFiles != null && !listFiles.isEmpty()) {
            try{
                for (File file : listFiles) {
                    MediaModel model  = new MediaModel(playlist.size() + 1, file.getName());
                    model.setPath(file.toURI().toASCIIString());
                    model.setSize(file.getTotalSpace() / 100/ 1024 / 1024 / 1024);

                    Media media = new Media(file.toURI().toASCIIString());
                    model.setDuration(media.getDuration().toString());

                    playlist.add(model);
                }
                initPlayer(playlist.get(0));
            } catch (Exception ex) {
                log(ex.getMessage());
            }
            
        }
    }

    private void creationTableView() {

        tableView = new TableView<MediaModel>();

        TableColumn<MediaModel, String> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numberColumn.setMaxWidth(40);
        numberColumn.setMinWidth(40);

        TableColumn<MediaModel, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<MediaModel, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        TableColumn<MediaModel, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        tableView.setItems(playlist);

        tableView.getColumns().add(numberColumn);
        tableView.getColumns().add(fileNameColumn);        
        tableView.getColumns().add(durationColumn);
        tableView.getColumns().add(sizeColumn);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                MediaModel mediaModel = tableView.getSelectionModel().getSelectedItem();
                initPlayer(mediaModel);
            }
        });
    }

    void createBottomActionBar() {
        playButton = new Button();
        playButton.setDisable(true);
        playButton.setGraphic(getImage("play.png"));
        playButton.setOnAction(e -> {
            if (endOfMedia) {
                player.stop();
                player.seek(player.getStartTime());
            }
            player.play();
            endOfMedia = false;
        });

        pauseButton = new Button();
        pauseButton.setDisable(true);
        pauseButton.setGraphic(getImage("pause.png"));
        pauseButton.setOnAction(e -> player.pause());

        stopButton = new Button();
        stopButton.setDisable(true);
        stopButton.setGraphic(getImage("stop.png"));
        stopButton.setOnAction(e -> player.stop());

        Button nextButton = new Button();
        nextButton.setGraphic(getImage("next.png"));
        nextButton.setOnAction(e -> {
            if (shuffleMode) {
                playShuffle();
            } else {
                playNext();
            }
        });

        Button previousButton = new Button();
        previousButton.setGraphic(getImage("previous.png"));
        previousButton.setOnAction(e -> {
            if (shuffleMode) {
                playShuffle();
            } else {
                playPrevious();
            }
        });

        HBox controlePPS = new HBox(3);
        controlePPS.getChildren().addAll(playButton, pauseButton, stopButton);

        HBox controlePN = new HBox(3);
        controlePN.getChildren().addAll(previousButton, nextButton);

        HBox controleButtons = new HBox(10);
        controleButtons.getChildren().addAll(controlePPS, controlePN);

        // Volume
        muteButton = new Button();
        muteButton.setGraphic(getImage("mute.png"));
        muteButton.setOnAction(e -> {
            sliderVolume.setValue(0);
            muteButton.setDisable(true);
            unmuteButton.setDisable(false);
        });

        unmuteButton = new Button();
        unmuteButton.setGraphic(getImage("audio.png"));
        unmuteButton.setOnAction(e -> {
            sliderVolume.setValue(50);
            muteButton.setDisable(false);
            unmuteButton.setDisable(true);
        });

        sliderVolume = new Slider();
        sliderVolume.valueProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if(player != null){
                        player.setVolume(sliderVolume.getValue() / 100.0);
                    }
                });

        // ProgresBar
        progressBar = new Slider();
        progressBar.valueProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (player != null && progressBar.isPressed()) {
                        player.seek(player.getTotalDuration().multiply(progressBar.getValue() / 100.0));
                    }
                });

        HBox audioLayout = new HBox(3);
        audioLayout.getChildren().addAll(muteButton, unmuteButton, sliderVolume);
        audioLayout.setAlignment(Pos.CENTER);

        HBox container = new HBox(10);
        container.getChildren().addAll(controleButtons, audioLayout);
        container.setAlignment(Pos.CENTER);

        bottomLayout = new VBox(10);
        bottomLayout.setPadding(new Insets(20));
        bottomLayout.setStyle("-fx-background-color: white");
        bottomLayout.getChildren().addAll(progressBar, container);

        rootLayout.setBottom(bottomLayout);
    }

    void createTopMenuBar() {
        // create all menu
        MenuBar menuBar = new MenuBar();

        // Media Menu
        Menu mediaMenu = new Menu("Media");

        MenuItem openFile = new MenuItem("Open File");
        openFile.setOnAction(e -> openFile());

        MenuItem openFiles = new MenuItem("Open Mutiple Files");
        openFiles.setOnAction(e -> getFiles());

        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> Platform.exit());

        mediaMenu.getItems().addAll(openFile, openFiles, quit);
        // End Media Menu

        // Options Menu
        Menu optionMenu = new Menu("Options");
        singleLoop = false;
        globalLoop = false;
        shuffleMode = false;

        MenuItem singleLoopItem = new MenuItem("Single Loop");
        singleLoopItem.setOnAction(e -> {
            if (singleLoop){
                singleLoop = false;
                singleLoopItem.setText("Single Loop");
            } else{
                singleLoop = true;
                singleLoopItem.setText("Desactivate Single Loop");
            }
        });

        MenuItem globalLoopItem = new MenuItem("Loop");
        globalLoopItem.setOnAction(e -> {
            if (globalLoop){
                singleLoop = false;
                globalLoopItem.setText("Loop");
            } else{
                globalLoop = true;
                globalLoopItem.setText("Desactivate Loop");
            }
        });

        MenuItem shuffleItem = new MenuItem("Shuffle");
        shuffleItem.setOnAction(e -> {
            if (shuffleMode){
                shuffleMode = false;
                alreadyPlay.clear();
                shuffleItem.setText("Shuffle");
            } else{
                shuffleMode = true;
                shuffleItem.setText("Desactivate Shuffle");
            }
        });

        optionMenu.getItems().addAll(singleLoopItem, globalLoopItem, shuffleItem);
        // End Option Menu

        // Video
        Menu viewMenu = new Menu("View");

        MenuItem playlistItem = new MenuItem("Playlist");
        playlistItem.setOnAction(e -> {
            rootLayout.setCenter(tableView);
        });
        MenuItem mediItem = new MenuItem("Media");
        mediItem.setOnAction(e -> {
            rootLayout.setCenter(mediaView);
        });        
        
        MenuItem fllScreen = new MenuItem("Full Screen (CTR+F)");
        fllScreen.setOnAction(e -> {
            stage.setFullScreen(true);
            updateViewsSize();
        });
        MenuItem normalScreen = new MenuItem("Normal Screen (CTR+N)");
        normalScreen.setOnAction(e -> {
            stage.setFullScreen(false);
            updateViewsSize();
        });

        viewMenu.getItems().addAll(playlistItem, mediItem, new SeparatorMenuItem(), fllScreen, normalScreen);

        // Settings
        Menu settingsMenu = new Menu("Settings");

        MenuItem bgr = new MenuItem("Change background");
        settingsMenu.getItems().add(bgr);

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(e -> {
            Color color = colorPicker.getValue();
            rootLayout.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        });
        bgr.setGraphic(colorPicker);

        Menu authorMenu = new Menu("Author");
        authorMenu.getItems().addAll(
            new MenuItem("Aliano CHARLES"),            
            new MenuItem("Berthin PIERRISTAL"),
            new MenuItem("Mackendy ALEXIS"),
            new MenuItem("Jean Duckens Sanon"),
            new MenuItem("Louis Midson LAJEANTY"),
            new MenuItem("Rolbert APHAON")
        );
        menuBar.getMenus().addAll(mediaMenu, optionMenu, viewMenu, settingsMenu, authorMenu);
        rootLayout.setTop(menuBar);
    }

    public static void main(String[] args) {
        launch();
    }

}