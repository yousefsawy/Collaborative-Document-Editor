package org.example.texteditor.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
public class DocumentCreateResponse {
    private String documentId;
    private String documentName;
    private String editorId;
    private String viewerId;
}
