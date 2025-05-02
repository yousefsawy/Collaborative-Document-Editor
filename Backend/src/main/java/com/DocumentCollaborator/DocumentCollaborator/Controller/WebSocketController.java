package com.DocumentCollaborator.DocumentCollaborator.Controller;

import CRDT.Node;
import CRDT.Operation;
import com.DocumentCollaborator.DocumentCollaborator.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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

    @MessageMapping("/connect/{documentId}")
    @SendToUser("/response/connect")
    public Node[] handleConnect(
        @DestinationVariable String documentId,
        @Payload String username,
        SimpMessageHeaderAccessor headerAccessor) {
        
        documentService.addUserToDocument(documentId, username);
        
        return documentService.getDocumentNodes(documentId);
    }
    
    @MessageMapping("/request/users/{documentId}")
    public void handleUserListRequest(@DestinationVariable String documentId) {
        System.out.println("User list requested for document: " + documentId);
        messagingTemplate.convertAndSend(
            "/topic/users/" + documentId,
            documentService.getDocumentUsers(documentId)
        );
    }

    @MessageMapping("/disconnect/{documentId}")
    public void handleDisconnect(@DestinationVariable String documentId, SimpMessageHeaderAccessor headerAccessor) {
        // Retrieve the user's session ID or username
        String userId = headerAccessor.getSessionId(); // Or use a custom header for username

        // Remove the user from the document
        documentService.removeUserFromDocument(documentId, userId);

        // Broadcast the updated user list to all users in the document
        messagingTemplate.convertAndSend("/topic/users/" + documentId, documentService.getDocumentUsers(documentId));
    }

        @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("WebSocket disconnected: " + sessionId);

        // Remove the user from all documents they are part of
        documentService.removeUserFromAllDocuments(sessionId);

        // Broadcast the updated user lists for all affected documents
        documentService.getAffectedDocuments(sessionId).forEach(documentId -> {
            messagingTemplate.convertAndSend("/topic/users/" + documentId, documentService.getDocumentUsers(documentId));
        });
    }
}