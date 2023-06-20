module com.finals2.mediaplayer {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.finals2.mediaplayer to javafx.fxml;

    exports com.finals2.mediaplayer;
}
