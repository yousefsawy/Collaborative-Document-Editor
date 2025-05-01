package org.example.texteditor.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
