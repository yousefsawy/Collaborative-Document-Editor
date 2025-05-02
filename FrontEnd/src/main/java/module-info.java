module org.example.texteditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.websocket;
    requires spring.messaging;
    requires spring.web;

    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens org.example.texteditor to spring.core, spring.beans, spring.context;
    opens org.example.texteditor.DTO to spring.core;
    opens org.example.texteditor.JavaFxControllers to javafx.fxml, spring.core, spring.beans;

    exports org.example.texteditor;
    exports org.example.texteditor.DTO;
    exports org.example.texteditor.JavaFxControllers;
    exports CRDT to com.fasterxml.jackson.databind;

}
