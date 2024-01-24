module com.ajikhoji.piano {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;


    opens com.ajikhoji.piano to javafx.fxml;
    exports com.ajikhoji.piano;
}