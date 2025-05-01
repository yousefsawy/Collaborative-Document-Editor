package com.DocumentCollaborator.DocumentCollaborator.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCreateRequest {
    private String title;   // required
    private String username; // required
    private String content; // optional
}
