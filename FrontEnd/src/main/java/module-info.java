module org.example.texteditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires spring.websocket;
    requires spring.messaging;

    opens org.example.texteditor to javafx.fxml;
    exports org.example.texteditor;
    exports org.example.texteditor.JavaFxControllers;
    opens org.example.texteditor.JavaFxControllers to javafx.fxml;
}