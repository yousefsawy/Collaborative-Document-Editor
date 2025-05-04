package org.example.texteditor.JavaFxControllers;

import CRDT.Node;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.texteditor.DTO.DocumentCreateRequest;
import org.example.texteditor.DTO.DocumentCreateResponse;
import org.example.texteditor.DTO.User;
import org.example.texteditor.WebSocketHandler.WebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CreateDocumentController {
    public static final String BASE_URL="http://localhost:8080/";
    RestTemplate restTemplate;
    private String username;
    WebSocketHandler webSocketHandler;
    String documentId;
    User[] users;

    Node[] nodes;

    @FXML
    private TextField documentTitleField;

    @FXML
    private TextArea documentContentArea;

    @FXML
    private Button importFileButton;

    @FXML
    private Button createDocumentButton;

    @FXML
    private Label welcomeLabel;
    private String editorId;
    private String viewerId;

    public void initialize(WebSocketHandler webSocketHandler, String username) {
        this.webSocketHandler = webSocketHandler;
        restTemplate = new RestTemplate();
        setWelcomeUsername(username);
    }

    public void setWelcomeUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome, " + username);
    }

    // Handle importing a text file
    @FXML
    private void handleImportFile() {
        // Open file chooser to select a text file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
                documentContentArea.setText(content.toString()); // Set content to the text area
            } catch (IOException e) {
                showError("Error importing file", "An error occurred while reading the file.");
            }
        }
    }

    // Handle creating the document
    @FXML
    private void handleCreateDocument() {
        String title = documentTitleField.getText();
        String content = documentContentArea.getText();

        if (title == null || title.trim().isEmpty()) {
            showError("Validation Error", "Document title is required.");
            return;
        }

        // Proceed with document creation logic
        System.out.println("Document Created with Title: " + title);
        System.out.println("Document Content: " + content);

        try {
            DocumentCreateRequest request = new DocumentCreateRequest(title,username,content);

            ResponseEntity<DocumentCreateResponse> responseDocument = restTemplate.postForEntity(
                    BASE_URL + "create", request, DocumentCreateResponse.class
            );



            if (responseDocument.getStatusCode().is2xxSuccessful()) {
                DocumentCreateResponse body = responseDocument.getBody();


                if (body == null || body.getDocumentId() == null) {
                    showError("Creation Failed", "Server returned an invalid response.");
                    return;
                }

                // Show success
                System.out.println("Document Created with ID: " + body.getDocumentId());
                this.documentId = body.getDocumentId();
                this.editorId = body.getEditorId();
                this.viewerId = body.getViewerId();


                ResponseEntity<User[]> responseUsers = restTemplate.getForEntity(
                        BASE_URL + "users/" + documentId, User[].class
                );

                users = responseUsers.getBody();

                webSocketHandler = new WebSocketHandler();
                webSocketHandler.connectToWebSocket();

                webSocketHandler.connectToDocumentAsync(body.getDocumentId(), (Node[] receivedNodes) -> {
                    this.nodes = receivedNodes;
                    Platform.runLater(this::openEditDocumentForm);
                });


            } else if (responseDocument.getStatusCode().is4xxClientError()) {
                showError("Client Error", "Check your request. Something is wrong on your end.");
            } else if (responseDocument.getStatusCode().is5xxServerError()) {
                showError("Server Error", "Server encountered a problem. Try again later.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Connection Error", "Could not connect to the server.");
        }
    }

    private void openEditDocumentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/texteditor/edit-view.fxml"));
            Scene editScene = new Scene(loader.load());

            EditController editController = loader.getController();
            editController.initialize(nodes, webSocketHandler, documentId, username, users,true,editorId, viewerId);

            // Get the current window and set the new scene
            Stage stage = (Stage) createDocumentButton.getScene().getWindow();
            stage.setScene(editScene);
            stage.setTitle("Edit Document");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Error opening Edit Document form.");
        }
    }


    // Helper method to show error dialogs
    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
