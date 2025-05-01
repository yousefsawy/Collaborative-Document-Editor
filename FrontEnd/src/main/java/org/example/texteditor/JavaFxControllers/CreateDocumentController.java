package org.example.texteditor.JavaFxControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.texteditor.DTO.DocumentCreateRequest;
import org.example.texteditor.DTO.DocumentCreateResponse;
import org.example.texteditor.WebSocketHandler.WebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CreateDocumentController {
    static final String BASE_URL="http://localhost:8080/";
    RestTemplate restTemplate = new RestTemplate();
    WebSocketHandler webSocketHandler = new WebSocketHandler();
    private String username;

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

    // Set username dynamically from another part of the application
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

            ResponseEntity<DocumentCreateResponse> response = restTemplate.postForEntity(
                    BASE_URL + "create", request, DocumentCreateResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                DocumentCreateResponse body = response.getBody();
                if (body == null || body.getDocumentId() == null) {
                    showError("Creation Failed", "Server returned an invalid response.");
                    return;
                }

                // Show success
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Document Created");
                successAlert.setContentText("Document ID: " + body.getDocumentId());
                successAlert.showAndWait();
                System.out.println("Document Created with ID: " + body.getDocumentId());
                webSocketHandler.connectToDocument(body.getDocumentId());

            } else if (response.getStatusCode().is4xxClientError()) {
                showError("Client Error", "Check your request. Something is wrong on your end.");
            } else if (response.getStatusCode().is5xxServerError()) {
                showError("Server Error", "Server encountered a problem. Try again later.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Connection Error", "Could not connect to the server.");
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
