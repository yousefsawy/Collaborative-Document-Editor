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
import org.example.texteditor.models.User;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

public class LoginController {
    private Node[] nodes;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField documentIdField;

    WebSocketHandler webSocketHandler;

    public void initialize() {
        System.out.println("Initializing Login Controller");
        webSocketHandler = new WebSocketHandler();
        webSocketHandler.connectToWebSocket();
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
            createDocController.initialize(webSocketHandler, username);

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

        // Connect to the document and fetch nodes
        webSocketHandler.connectToDocumentAsync(documentId, usernameField.getText(), (Node[] receivedNodes) -> {
            this.nodes = receivedNodes;
            Platform.runLater(this::openEditDocumentForm);
        });
    }

    private void openEditDocumentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/texteditor/edit-view.fxml"));
            Scene editScene = new Scene(loader.load());
    
            EditController editController = loader.getController();
            editController.initialize(nodes, webSocketHandler, documentIdField.getText(), usernameField.getText());
        
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
