package com.DocumentCollaborator.DocumentCollaborator.Controller;

import CRDT.Node;
import CRDT.Operation;
import com.DocumentCollaborator.DocumentCollaborator.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @Autowired
    private final DocumentService documentService;
    public WebSocketController(DocumentService documentService) {
        this.documentService = documentService;
    }


    // Handles operations, Should return an operation object
    @MessageMapping("/operation/{documentId}")
    @SendTo("/topic/operation/{documentId}")
    public String handleOperation(String message, @DestinationVariable String documentId) {
        System.out.println("Received message in room " + documentId + ": " + message);
        return message;
    }

    //Handles connections to document, Should return nodes to build tree
    @MessageMapping("/connect/{documentId}")
    @SendToUser("/topic/connect/{documentId}")
    public Node[] handleConnect(@DestinationVariable String documentId) {
        System.out.println("Received message in room " + documentId);
        return documentService.getDocumentNodes(documentId);
    }
}
