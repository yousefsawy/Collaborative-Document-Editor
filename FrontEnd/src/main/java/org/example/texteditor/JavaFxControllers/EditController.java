package org.example.texteditor.JavaFxControllers;

import CRDT.CRDT_TREE;
import CRDT.Node;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.example.texteditor.models.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.texteditor.WebSocketHandler.WebSocketHandler;

import org.example.texteditor.components.RichTextEditor;
public class EditController {
    private CRDT_TREE tree;
    private WebSocketHandler webSocketHandler;

    @FXML private VBox usersList;
    private RichTextEditor richTextArea; // Replace CustomTextArea
    private TextArea documentContentArea; // Keep reference to the actual text area

    private boolean hasRequestedInitialUsers = false;

    public void initialize(Node[] nodes, WebSocketHandler webSocketHandler, String documentId, String username) {
        this.webSocketHandler = webSocketHandler;
    
        this.documentContentArea = richTextArea.getTextArea();
        // Initialize the CRDT tree
        tree = new CRDT_TREE("Doc", username, nodes);
        documentContentArea.setText(tree.getDocument());
    
        // Subscribe to the user list topic for real-time updates
        webSocketHandler.subscribeToTopic("/topic/users/" + documentId, message -> {
            Platform.runLater(() -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    User[] users = mapper.readValue(message, User[].class);
                    updateUserList(users);
                } catch (Exception e) {
                    System.err.println("Failed to parse user update:");
                    e.printStackTrace();
                }
            });
        });
    
        // Request initial user list ONCE
        if (!hasRequestedInitialUsers) {
            System.out.println("Requesting initial user list for document: " + documentId);
            webSocketHandler.sendMessage("/app/request/users/" + documentId, "");
            hasRequestedInitialUsers = true;
        }
    }

    public void updateUserList(User[] users) {
        Platform.runLater(() -> {
            usersList.getChildren().clear();
            for (User user : users) {
                Label userLabel = new Label(user.toString());
                userLabel.setStyle("-fx-padding: 5; -fx-background-color: white; " +
                                 "-fx-border-color: gray; -fx-border-radius: 5;");
                usersList.getChildren().add(userLabel);
            }
    
            updateCursors(users);
        });
    }
    
    private void updateUserCursors(User[] users) {
        // Remove disappeared users
        Set<String> currentUsers = Arrays.stream(users)
            .map(User::getUserId)
            .collect(Collectors.toSet());
        
        userCarets.keySet().removeIf(id -> !currentUsers.contains(id));
        
        // Add/update carets
        for (User user : users) {
            if (!userCarets.containsKey(user.getUserId())) {
                richTextArea.addUserCaret(
                    user.getUserId(), 
                    Color.web(user.getCursorColor())
                );
            }
            richTextArea.updateCaretPosition(
                user.getUserId(),
                user.getCursorPosition()
            );
        }
    }

    // Keep your existing CRDT tree methods
    public CRDT_TREE getTree() {
        return tree;
    }
}