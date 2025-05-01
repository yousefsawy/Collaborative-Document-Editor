package com.DocumentCollaborator.DocumentCollaborator.Controller;

import CRDT.Operation;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // Handles operations, Should return an operation object
    @MessageMapping("/operation/{documentId}")
    @SendTo("/topic/operation/{documentId}")
    public Operation handleOperation(String message, @DestinationVariable String documentId) {
        System.out.println("Received message in room " + documentId + ": " + message);
        return Operation;
    }

    //Handles connections to document, Should return nodes to build tree
    @MessageMapping("/connect/{documentId}")
    @SendTo("/topic/connect/{documentId}")
    public Nodes[] handleConnect(String message, @DestinationVariable String documentId) {
        System.out.println("Received message in room " + documentId + ": " + message);
        return message;
    }
}
