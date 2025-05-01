package org.example.texteditor.JavaFxControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CreateDocumentController {

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

        // You can send the data to a service or handle it as required
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
