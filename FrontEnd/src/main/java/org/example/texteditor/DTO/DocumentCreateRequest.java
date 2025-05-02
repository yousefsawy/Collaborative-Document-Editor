package org.example.texteditor.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCreateRequest {
    private String title;   // required
    private String username; // required
    private String content; // optional

    public DocumentCreateRequest(String title, String username, String content) {
        this.title = title;
        this.username = username;
        this.content = content;
    }
}
