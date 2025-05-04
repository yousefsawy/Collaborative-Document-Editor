package com.DocumentCollaborator.DocumentCollaborator.Model;
import CRDT.CRDT_TREE;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import CRDT.Node;
import CRDT.Operation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Document {
    String documentId;
    String editorId;
    String viewerId;
    String documentName;

    @JsonIgnore
    ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String,User>();

    @JsonIgnore
    CRDT_TREE tree;


    public Document(String documentName, String username, String content) {
        initalizeVariables(documentName, username);
        initalizeTree(content);
    }

    public Document(String documentName, String username) {
        initalizeVariables(documentName, username);
    }

    private void initalizeVariables(String documentName, String username)
    {
        this.documentId = UUID.randomUUID().toString();
        this.editorId = UUID.randomUUID().toString();
        this.viewerId = UUID.randomUUID().toString();
        this.documentName = documentName;
        tree = new CRDT_TREE(documentName, username);
    }

    private void initalizeTree(String content) {
        tree.localInsert(0, content, System.currentTimeMillis());
    }

    public Node[] getDocumentNodes() {
        return tree.sendTree();
    }

    public void handleOperation(Operation operation) {
        tree.remoteUpdate(operation);
    }

    public void setUsersFromArray(User[] usersArray) {
        // Clear the current users map and add the new ones
        User[] copyUsersArray = usersArray.clone();
        Integer counter = 0;
        users.clear();
        for (User user : usersArray) {
            User newUser = new User(user.getUsername(), copyUsersArray[counter++].getCaretColor());
            users.put(newUser.getUserId(), newUser);
        }
    }

}
