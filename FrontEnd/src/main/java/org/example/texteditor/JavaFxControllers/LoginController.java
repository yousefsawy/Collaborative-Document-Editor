package org.example.texteditor.JavaFxControllers;

import CRDT.Node;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.example.texteditor.WebSocketHandler.WebSocketHandler;

import java.io.IOException;

public class LoginController {
    private Node[] nodes;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField documentIdField;

    public void initialize() {
        System.out.println("Initializing Login Controller");
    }

    public void handleCreateDocument() {
        String username = usernameField.getText();

        if (username == null || username.trim().isEmpty()) {
            showError("Username is required");
            return;
        }

        openCreateDocumentForm(username);
    }

    private void openCreateDocumentForm(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/texteditor/create-view.fxml"));
            Scene createDocumentScene = new Scene(loader.load());

            CreateDocumentController createDocController = loader.getController();
            createDocController.setWelcomeUsername(username);

            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(createDocumentScene);
            currentStage.setTitle("Create Document");
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening Create Document form");
        }
    }

    public void handleJoinDocument() {
        String documentId = documentIdField.getText();

        if (documentId == null || documentId.trim().isEmpty()) {
            showError("Document ID is required");
            return;
        }

        WebSocketHandler webSocketHandler = new WebSocketHandler();
        webSocketHandler.connectToWebSocket();
        // webSocketHandler.connectToDocument(documentIdField.getText());

        webSocketHandler.connectToDocumentAsync(documentId, (Node[] receivedNodes) -> {
            this.nodes = receivedNodes;
            Platform.runLater(this::openEditDocumentForm);
        });


//        nodes = webSocketHandler.getNodes();
//
//        while(nodes == null)
//        {
//            nodes = webSocketHandler.getNodes();
//        }
//
//        openEditDocumentForm();


    }

    private void openEditDocumentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/texteditor/edit-view.fxml"));
            Scene editScene = new Scene(loader.load());

            EditController editController = loader.getController();
            editController.initialize(nodes);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(editScene);
            stage.setTitle("Edit Document");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening Edit Document form");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
