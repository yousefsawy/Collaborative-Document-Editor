package com.DocumentCollaborator.DocumentCollaborator.Controller;
import CRDT.Node;
import com.DocumentCollaborator.DocumentCollaborator.DTO.DocumentCreateRequest;
import com.DocumentCollaborator.DocumentCollaborator.DTO.DocumentCreateResponse;
import com.DocumentCollaborator.DocumentCollaborator.Model.Document;
import com.DocumentCollaborator.DocumentCollaborator.Model.User;
import com.DocumentCollaborator.DocumentCollaborator.Service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;


@RestController
public class DocumentRestController {
    private final DocumentService documentService;

    public DocumentRestController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/create")
    public DocumentCreateResponse createDocument(@RequestBody DocumentCreateRequest request) {
        if (request.getTitle() == null || request.getUsername() == null || request.getUsername().isBlank() || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and UserId are required");
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            System.out.println(request.getContent());
            return documentService.createDocument(request.getTitle(), request.getUsername(), request.getContent());
        } else {
            System.out.println(request.getTitle());
            return documentService.createDocument(request.getTitle(), request.getUsername());
        }
    }

    @GetMapping("/users/{documentId}")
    public User[] getDocumentUsers(@PathVariable String documentId) {
        System.out.println("DocumentId: " + documentId);
        return documentService.getDocumentUsers(documentId);
    }


    // TESTING PURPOSES ONLY SHOULDN'T BE CALLED ON CLIENT SIDE
    @GetMapping("/{documentId}")
    public Node[] getDocument(@PathVariable String documentId) {
        System.out.println("DocumentId: " + documentId);
        return documentService.getDocumentNodes(documentId);
    }
}


