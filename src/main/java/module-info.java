module com.finals2.mediaplayer {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.finals2.mediaplayer to javafx.fxml;
    exports com.finals2.mediaplayer;
}
