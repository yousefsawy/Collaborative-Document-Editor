package com.DocumentCollaborator.DocumentCollaborator;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // Handles insert text to "/insert/{documentId}" destination
    @MessageMapping("/insert/{documentId}")
    @SendTo("/topic/insert/{documentId}")
    public String handleInsert(String message, @DestinationVariable String documentId) {
        System.out.println("Received message in room " + documentId + ": " + message);
        return message;
    }

//
//    // Handles delete text to "/delete/{documentId}" destination
//    @MessageMapping("/delete/{documentId}")
//    @SendTo("/topic/delete/{documentId}")
//    public String handleDelete(String message, @DestinationVariable String documentId) {
//        System.out.println("Received message in room " + documentId + ": " + message);
//        return message;
//    }
//
//    // Handles undo text to "/undo/{documentId}" destination
//    @MessageMapping("/undo/{documentId}")
//    @SendTo("/topic/undo/{documentId}")
//    public String handleUndo(@DestinationVariable String documentId) {
//        System.out.println("Received message in room " + documentId + ": " + "message");
//        return "message";
//    }
//
//    // Handles redo text to "/redo/{documentId}" destination
//    @MessageMapping("/redo/{documentId}")
//    @SendTo("/topic/redo/{documentId}")
//    public String handleRedo(@DestinationVariable String documentId) {
//        System.out.println("Received message in room " + documentId + ": " + "message");
//        return "message";
//    }
}
