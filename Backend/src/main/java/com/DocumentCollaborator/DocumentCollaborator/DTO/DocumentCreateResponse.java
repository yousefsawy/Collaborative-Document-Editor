package com.DocumentCollaborator.DocumentCollaborator.DTO;


import lombok.Getter;

@Getter
public class DocumentCreateResponse {
    private String documentId;
    private String documentName;
    private String editorId;
    private String viewerId;

    public DocumentCreateResponse(String documentId, String documentName, String editorId, String viewerId) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.editorId = editorId;
        this.viewerId = viewerId;
    }
}
