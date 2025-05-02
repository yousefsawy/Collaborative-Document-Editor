package com.DocumentCollaborator.DocumentCollaborator.Controller;

import CRDT.Node;
import CRDT.Operation;
import com.DocumentCollaborator.DocumentCollaborator.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private final DocumentService documentService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public WebSocketController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @MessageMapping("/operation/{documentId}")
    @SendTo("/topic/operation/{documentId}")
    public Operation handleOperation(@DestinationVariable String documentId, @Payload Operation Op) {
        System.out.println("Sending response operation: " + Op);
        documentService.handleDocumentOperation(documentId, Op);
        return Op;
    }

    // CALLED FIRST TIME A USER CONNECTS
    @MessageMapping("/connect/{documentId}")
    @SendToUser("/response/connect")
    public Node[] handleConnect(@DestinationVariable String documentId) {
        System.out.println("Sending response: Connection");
        // documentService.addUserToDocument(username, documentId); // TODO: COULD BE CHANGED TO A DIFFERENT POST REQUEST
        return documentService.getDocumentNodes(documentId);
    }

}