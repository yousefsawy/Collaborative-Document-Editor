package com.DocumentCollaborator.DocumentCollaborator.Model;
import CRDT.CRDT_TREE;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import CRDT.Node;
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
    String ownerId;

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

    public void addUser(String username) {
        User user = new User(username);
        users.put(user.userId, user);
    }

    public void removeUser(String id) {
        users.remove(id); // returns false if user was not in the set
    }

    private void initalizeVariables(String documentName, String username)
    {
        this.documentId = UUID.randomUUID().toString();
        this.editorId = UUID.randomUUID().toString();
        this.viewerId = UUID.randomUUID().toString();
        this.documentName = documentName;
        User owner = new User(username);
        this.ownerId = owner.userId;
        users.put(this.ownerId, owner);
    }

    private void initalizeTree(String content) {
        tree = new CRDT_TREE(UUID.randomUUID().toString());
        tree.localInsert(0, content, 0);
    }

    public Node[] getDocumentNodes() {

    }
}
