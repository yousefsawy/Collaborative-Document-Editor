package org.example.texteditor.JavaFxControllers;

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

    // Injecting TextFields from FXML
    @FXML
    private TextField usernameField;

    @FXML
    private TextField documentIdField;


    WebSocketHandler websocket;

    public void initialize() {
        System.out.println("Initializing Login Controller");
        websocket = new WebSocketHandler();
        websocket.connectToWebSocket();
    }

    public void handleCreateDocument() {
        System.out.println("Create Document");

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

            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening Create Document form");
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void handleJoinDocument() {}

}