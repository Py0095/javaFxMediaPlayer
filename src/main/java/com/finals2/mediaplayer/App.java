package com.finals2.mediaplayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private BorderPane rootLayout;
    private MediaPlayer player;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Media Player");

        rootLayout = new BorderPane();
        scene = new Scene(rootLayout, 640, 480);

        // Global
        createTopMenuBar();

        stage.setScene(scene);
        stage.show();
    }

    private void getFiles() {
        ObservableList<MediaModel> mediaList = FXCollections.observableArrayList();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Audio/Video", "*.mp3", "*.mp4", "*.flv", "*.3gp",
                "*.wma", "*.wav", "*.ogg", "*.wmv", "v.avg", "*.avi"));

        List<File> listFiles = fileChooser.showOpenMultipleDialog(null);

        if (listFiles != null && !listFiles.isEmpty()) {
            for (File file : listFiles) {
                mediaList.add(new MediaModel(listFiles.indexOf(file) + 1, file.getName()));
            }

            creationTableView(mediaList);
        }
    }

    private void creationTableView(ObservableList<MediaModel> mediaList) {

        TableView<MediaModel> tableView = new TableView<>();

        TableColumn<MediaModel, String> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MediaModel, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        tableView.setItems(mediaList);

        tableView.getColumns().addAll(numberColumn, fileNameColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        rootLayout.setLeft(tableView);
    }

    void createBottomActionBar() {

        Image playImage = new Image(getClass().getResourceAsStream("play.png"));
        Image pauseImage = new Image(getClass().getResourceAsStream("pause.png"));


        btnPlay_Pause.setGraphic(new ImageView(image_btnPlay_Pause));
        btnPlay_Pause.setOnAction(e -> {
            if (playerState == false) {
                btnPlay_Pause.setGraphic());
                if (player != null) {
                    playerState = !playerState;
                    player.play();
                    System.out.println("you've been clicked in play btn ");
                }

            } else {

                btnPlay_Pause.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("play.png"))));
                if (player != null) {
                    playerState = !playerState;
                    player.pause();
                }
            }
        });
        Button btn_Previous = new Button();
        btn_Previous.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("previous.png"))));
        btn_Previous.setOnAction(e -> {
            if (player != null) {
                System.out.println("you've been clicked in previous btn ");
            }
        });

        Button btn_stop = new Button();
        btn_stop.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("stop.png"))));
        btn_stop.setOnAction(e -> {
            if (player != null) {
                player.stop();
                btnPlay_Pause.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("play.png"))));

                System.out.println("you've been clicked in stop btn ");
            }
        });

        Button btn_next = new Button();
        btn_next.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("next.png"))));
        btn_next.setOnAction(e -> {
            if (player != null) {
                System.out.println("you've been clicked in next btn ");
            }
        });

        Button btnMute_unMute = new Button();
        btnMute_unMute.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("audio.png"))));
        btnMute_unMute.setOnAction(e -> {
            if (muteState == false) {
                btnMute_unMute.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("no-audio.png"))));

                if (player != null) {
                    muteState = !muteState;
                    player.setMute(true);
                    System.out.println("you've been clicked in mute btn ");
                }
            } else {
                btnMute_unMute.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("audio.png"))));
                if (player != null) {
                    muteState = !muteState;
                    player.setMute(false);
                }

            }
        });
        wrp_btnPlay_Pause.getChildren().addAll(btnPlay_Pause);
        wrp_btnPlay_Pause.setPadding(new Insets(10, 10, 15, 10));

        HBox wrp_prev_stop_next = new HBox(3);
        wrp_prev_stop_next.getChildren().addAll(btn_Previous, btn_stop, btn_next);
        wrp_prev_stop_next.setPadding(new Insets(10, 10, 15, 10));

        HBox wrp_mute = new HBox();

        vol.setPadding(new Insets(0, 10, 10, 10));
        wrp_mute.getChildren().addAll(btnMute_unMute);
        wrp_mute.setPadding(new Insets(10, 0, 0, 10));

        HBox wrp_volume = new HBox();
        wrp_volume.getChildren().add(vol);
        wrp_volume.setAlignment(Pos.CENTER);

        HBox container = new HBox();
        container.setStyle("-fx-background-color: white");
        container.getChildren().addAll(wrp_btnPlay_Pause, wrp_prev_stop_next, wrp_mute, wrp_volume);
        // timer.setPadding(new Insets(0,0,5,50));

        VBox vbox = new VBox();
        vbox.getChildren().addAll(time, container);
        root.setBottom(vbox);
    }

    void createTopMenuBar() {
        // create all menu
        MenuBar menuBar = new MenuBar();

        // Media
        Menu media = new Menu("Media");

        MenuItem openFile = new MenuItem("Open File");
        MenuItem openFiles = new MenuItem("Open Mutiple Files");
        openFiles.setOnAction(e -> getFiles());

        MenuItem quit = new MenuItem("Quit");
        media.getItems().addAll(openFile, openFiles, quit);

        // PlayBack
        Menu playback = new Menu("Playback");

        MenuItem title = new MenuItem("Title");
        MenuItem play = new MenuItem("Play");
        MenuItem stop = new MenuItem("Stop");
        MenuItem previous = new MenuItem("Previous");
        MenuItem next = new MenuItem("Next");
        MenuItem record = new MenuItem("Record");

        Menu speed = new Menu("Speed");
        MenuItem faster = new MenuItem("Faster(CTR +)");
        MenuItem normal = new MenuItem("Normal");
        MenuItem slow = new MenuItem("Slow(CTR -)");
        speed.getItems().addAll(faster, normal, slow);
        playback.getItems().addAll(title, speed, play, stop, previous, next, record);

        // Audio
        Menu audio = new Menu("Audio");

        MenuItem increaseVolume = new MenuItem("Increase Volume");
        MenuItem decreaseVolume = new MenuItem("Decrease Volume");
        MenuItem mute = new MenuItem("Mute");
        audio.getItems().addAll(increaseVolume, decreaseVolume, mute);

        // Video
        Menu video = new Menu("View");

        MenuItem fllScreen = new MenuItem("Full Screen (CTR+F)");
        MenuItem normalScreen = new MenuItem("Normal Screen (CTR+N)");
        video.getItems().addAll(fllScreen, normalScreen);

        // Settings
        Menu settings = new Menu("Settings");

        MenuItem bgr = new MenuItem("Change background");
        settings.getItems().add(bgr);

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(e -> {
            Color color = colorPicker.getValue();
            rootLayout.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        });
        bgr.setGraphic(colorPicker);

        Menu help = new Menu("Help");
        Menu about = new Menu("About");

        menuBar.getMenus().addAll(media, playback, audio, video, settings, help, about);
        rootLayout.setTop(menuBar);
    }

    public static void main(String[] args) {
        launch();
    }

}