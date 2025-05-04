package org.example.texteditor.JavaFxControllers;

import CRDT.CRDT_TREE;
import CRDT.Node;
import CRDT.Operation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.example.texteditor.DTO.User;
import org.example.texteditor.WebSocketHandler.WebSocketHandler;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EditController {

    public TextField ViewerIdFx;
    public TextField EditorIdFx;
    public Button undoButton;
    private CRDT_TREE tree;
    private WebSocketHandler webSocketHandler;
    private String documentId;
    private String userId;
    private boolean suppressListener;
    private Document swingDocument;
    String[] users;
    String username;
    boolean isEditor;

    @FXML
    private VBox usersList;

    @FXML
    private TextArea documentContentArea;

    public void initialize(Node[] nodes, WebSocketHandler webSocketHandler, String documentId, String username,String[] users, boolean isEditor, String editorId, String viewerId) {
        this.users = new String[users.length + 1];
        documentContentArea.setEditable(isEditor);
        // Copy existing users into the new array
        System.arraycopy(users, 0, this.users, 0, users.length);

        // Add the current username to the end of the array
        this.users[users.length] = username;
        this.username = username;
        System.out.println("username:" + " " + Arrays.toString(this.users));
        executeUserUpdate(this.users);

        // Populate the VBox with user items

        // Initialize the CRDT tree
        tree = new CRDT_TREE("Doc", username, nodes);
        this.webSocketHandler = webSocketHandler;
        this.documentId = documentId;
        this.userId = userId;
        System.out.println("Sending users " + Arrays.toString(this.users));
        webSocketHandler.sendUserUpdate(documentId, this.users);

        // Set initial text
        suppressListener = true;
        documentContentArea.setText(tree.getDocument());
        suppressListener = false;
        
        setupUndoableEditListener();
        subscribeToOperations();
        subscribeToUsers();

        Platform.runLater(() -> {
            Stage stage = (Stage) documentContentArea.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        });

        if (editorId != null && !editorId.isEmpty()) {
            EditorIdFx.setText(editorId);
        } else {
            EditorIdFx.setText("");
        }

        if (viewerId != null && !viewerId.isEmpty()) {
            ViewerIdFx.setText(viewerId);
        } else {
            ViewerIdFx.setText("");
        }

    }


    private void subscribeToOperations() {
        webSocketHandler.receiveDocumentOperation(documentId, this::executeOperationOnTree);
    }

    private void subscribeToUsers() {
        webSocketHandler.receiveUserUpdates(documentId, this::executeUserUpdate);
    }


    private void executeOperationOnTree(Operation receivedOperation) {
        // Save cursor position before update
        if (Objects.equals(receivedOperation.getUser(), username))
            return;
        int caretPosition = documentContentArea.getCaretPosition();
        
        // Apply the remote operation to the tree
        tree.remoteUpdate(receivedOperation);

        suppressListener = true;
        Platform.runLater(() -> {
            String updatedText = tree.getDocument();
            
            // Update text without affecting cursor
            documentContentArea.setText(updatedText);
            
            // Restore cursor position (with bounds checking)
            if (caretPosition >= 0 && caretPosition <= updatedText.length()) {
                documentContentArea.positionCaret(caretPosition);
            }
            
            suppressListener = false;
        });
    }

    private void executeUserUpdate(String[] updatedUsers) {
        this.users = updatedUsers;
        System.out.println("Recieved users " + Arrays.toString(this.users));

        Platform.runLater(() -> {
            usersList.getChildren().clear();

            for (String user : updatedUsers) {
                Label userLabel = new Label(user);
                userLabel.setStyle("-fx-padding: 5; -fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 5; -fx-margin: 5;");
                usersList.getChildren().add(userLabel);
            }
        });
    }

    private void handleWindowClose(WindowEvent event) {
        System.out.println("Window closing, removing user: " + username);
        removeUser(username);
        webSocketHandler.close();
    }


    private void removeUser(String username) {
        List<String> userList = new ArrayList<>(List.of(users)); // Convert to list for easier removal
        userList.remove(username); // Remove the username

        users = userList.toArray(new String[0]);

        webSocketHandler.sendUserUpdate(documentId, users);

        // Update the UI
        executeUserUpdate(users);
    }


    private void setupUndoableEditListener() {
        documentContentArea.textProperty().addListener((obs, oldText, newText) -> {
            if (suppressListener) return;
    
            // Get cursor position before the change
            int caretPosition = documentContentArea.getCaretPosition();
            int oldLength = oldText.length();
            
            // Process the change
            if (oldText.length() < newText.length()) {
                // Insertion
                int diffIndex = findFirstDifferenceIndex(oldText, newText);
                int insertedLength = newText.length() - oldText.length();
                String insertedText = newText.substring(diffIndex, diffIndex + insertedLength);
                
                // Insert each character individually
                long timestamp = System.currentTimeMillis();
                for (int i = 0; i < insertedText.length(); i++) {
                    char c = insertedText.charAt(i);
                    System.out.println("inserted char: " + String.valueOf(c) + ", timestamp: " + timestamp + "Position: " + diffIndex + i);
                    Operation operation = tree.localInsert(diffIndex + i, String.valueOf(c), timestamp);
                    if (operation != null) {
                        webSocketHandler.sendDocumentOperation(documentId, operation);
                    }
                }
                
                // Adjust caret position after insertion
                Platform.runLater(() -> {
                    documentContentArea.positionCaret(diffIndex + insertedLength);
                });
    
            } else if (oldText.length() > newText.length()) {
                // Deletion
                int diffIndex = findFirstDifferenceIndex(oldText, newText);
                int deletionLength = oldText.length() - newText.length();
                
                // Delete characters one by one
                for (int i = 0; i < deletionLength; i++) {
                    Operation operation = tree.localDeleteOne(diffIndex + 1);
                    System.out.println("Deleted on charachter: " + diffIndex + 1);
                    if (operation != null) {
                        webSocketHandler.sendDocumentOperation(documentId, operation);
                    }
                }
                
                // Adjust caret position after deletion
                Platform.runLater(() -> {
                    documentContentArea.positionCaret(diffIndex);
                });
    
            } else if (!oldText.equals(newText)) {
                // Replacement (same length, different content)
                int diffStart = findFirstDifferenceIndex(oldText, newText);
                int diffEnd = findLastDifferenceIndex(oldText, newText, diffStart);
                
                // First delete old characters
                for (int i = diffStart; i <= diffEnd; i++) {
                    Operation operation = tree.localDeleteOne(diffStart);
                    if (operation != null) {
                        webSocketHandler.sendDocumentOperation(documentId, operation);
                    }
                }
                
                // Then insert new characters
                long timestamp = System.currentTimeMillis();
                String insertedText = newText.substring(diffStart, diffEnd + 1);
                for (int i = 0; i < insertedText.length(); i++) {
                    char c = insertedText.charAt(i);
                    Operation operation = tree.localInsert(diffStart + i, String.valueOf(c), timestamp);
                    if (operation != null) {
                        webSocketHandler.sendDocumentOperation(documentId, operation);
                    }
                }
                
                // Adjust caret position after replacement
                Platform.runLater(() -> {
                    documentContentArea.positionCaret(diffStart + insertedText.length());
                });
            }
        });
    }
        
    private int findFirstDifferenceIndex(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return i;
            }
        }
        
        // If no difference found, the difference is at the end
        if (str1.length() != str2.length()) {
            return minLength;
        }
        
        return -1; // No difference
    }
    
    private int findLastDifferenceIndex(String str1, String str2, int startFrom) {
        // Find the last position where characters differ
        int pos1 = str1.length() - 1;
        int pos2 = str2.length() - 1;
        
        // Work backwards from the end until we find a difference
        while (pos1 >= startFrom && pos2 >= startFrom) {
            if (str1.charAt(pos1) != str2.charAt(pos2)) {
                return Math.max(pos1, pos2);
            }
            pos1--;
            pos2--;
        }
        
        // If we get here, the difference continues to the startFrom position
        return startFrom;
    }

    @FXML
    private void undoButton() {
        Operation undoOp = tree.undo();
        if (undoOp != null) {
            suppressListener = true;
            documentContentArea.setText(tree.getDocument());
            suppressListener = false;

            webSocketHandler.sendDocumentOperation(documentId, undoOp);
        }
    }

    @FXML
    private void redoButton() {
        Operation redoOp = tree.redo();
        if (redoOp != null) {
            suppressListener = true;
            documentContentArea.setText(tree.getDocument());
            suppressListener = false;

            webSocketHandler.sendDocumentOperation(documentId, redoOp);
        }
    }
}