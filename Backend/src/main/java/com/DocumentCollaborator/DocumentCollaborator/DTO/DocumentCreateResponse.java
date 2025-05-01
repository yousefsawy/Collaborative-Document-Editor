package com.DocumentCollaborator.DocumentCollaborator.DTO;


import lombok.Getter;

@Getter
public class DocumentCreateResponse {
    private String documentId;
    private String documentName;
    private String editorId;
    private String viewerId;
    private String userId; // OWNER

    public DocumentCreateResponse(String documentId, String documentName, String editorId, String viewerId, String userId) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.editorId = editorId;
        this.viewerId = viewerId;
        this.userId = userId;
    }
}
