package org.example.texteditor.JavaFxControllers;

import CRDT.CRDT_TREE;
import CRDT.Node;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;

public class EditController {

    CRDT_TREE tree;

    @FXML
    private VBox usersList;

    @FXML
    private TextArea documentContentArea;


    public void initialize(Node[] nodes) {
        // Example: Fetch dynamic list of users
        List<String> users = fetchUsers();

        // Populate the VBox with user items
        for (String user : users) {
            Label userLabel = new Label(user);
            // Apply custom styles to the label
            userLabel.setStyle("-fx-padding: 5; -fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 5;");
            userLabel.setOnMouseClicked(event -> handleUserSelection(user));
            usersList.getChildren().add(userLabel);
        }
        tree = new CRDT_TREE("Doc", "UserName", nodes);
        documentContentArea.setText(tree.getDocument());
    }

    private List<String> fetchUsers() {
        // Replace this with actual data fetching logic
        return List.of("User 1", "User 2", "User 3", "User 4");
    }

    private void handleUserSelection(String user) {
        // Handle user selection (e.g., display user details)
        System.out.println("Selected user: " + user);
    }
}