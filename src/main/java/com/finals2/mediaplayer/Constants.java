package com.finals2.mediaplayer;

import javafx.stage.FileChooser.ExtensionFilter;

public class Constants {
    
    public static ExtensionFilter getExtensionFilter() {
        return new ExtensionFilter("Audio/Video", "*.mp3", "*.mp4", "*.flv", "*.3gp", "*.wma", "*.wav", "*.ogg", "*.wmv", "*.avg", "*.avi");
    }


}
