package org.example.texteditor.WebSocketHandler;

import CRDT.Node;
import CRDT.Operation;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.example.texteditor.DTO.User;
import org.example.texteditor.WebSocketHandler.FrameHandlers.NodeArrayFrameHandler;
import org.example.texteditor.WebSocketHandler.FrameHandlers.OperationFrameHandler;
import org.example.texteditor.WebSocketHandler.FrameHandlers.UserArrayFrameHandler;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class WebSocketHandler {
    private StompSession stompSession;

    // Session handler with proper error tracking
    private class ConnectionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("=== CONNECTED to WebSocket ===");
            System.out.println("Session ID: " + session.getSessionId());
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            System.err.println("=== ERROR in session ===");
            System.err.println("Command: " + command);
            System.err.println("Exception: " + exception.getMessage());
            exception.printStackTrace();
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("=== TRANSPORT ERROR ===");
            System.err.println("Error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public boolean connectToWebSocket() {
        try {
            System.out.println("========================================");
            System.out.println("WEBSOCKET: Attempting to connect...");

            // Create SockJS client
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            // Create STOMP client with Jackson converter for JSON objects
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

            // Use MappingJackson2MessageConverter for handling complex objects
            MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
            stompClient.setMessageConverter(converter);

            // Connect to the WebSocket server
            String url = "ws://localhost:8080/ws";

            this.stompSession = stompClient
                    .connectAsync(url, new WebSocketHttpHeaders(), new ConnectionHandler())
                    .get(10, TimeUnit.SECONDS);

            boolean isConnected = this.stompSession != null && this.stompSession.isConnected();
            System.out.println("WEBSOCKET: Connection status: " + (isConnected ? "CONNECTED" : "FAILED"));
            System.out.println("========================================");

            return isConnected;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("WEBSOCKET: Connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /* ----------------------------- DOCUMENT CONNECTION HANDLER ----------------------------------------------- */
    public void connectToDocumentAsync(String documentId, Consumer<Node[]> connectCallback) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: Cannot subscribe - not connected");
            return;
        }

        try {
            NodeArrayFrameHandler NodeframeHandler = new NodeArrayFrameHandler(connectCallback);

            // Subscribe to the Node Receiver to Build the Tree
            stompSession.subscribe("/user/response/connect", NodeframeHandler);

            // Trigger server-side connect logic
            stompSession.send("/app/connect/" + documentId, "");

        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error connecting to document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ----------------------------- DOCUMENT OPERATION HANDLERS ----------------------------------------------- */
    public void sendDocumentOperation(String documentId, Operation operation) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: not connected");
            return;
        }
        try {
            // Trigger server-side operation login
            stompSession.send("/app/operation/" + documentId, operation);
        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error connecting to document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void receiveDocumentOperation(String documentId, Consumer<Operation> operationCallback) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: not connected");
            return;
        }
        try {
            // Trigger server-side operation login
            stompSession.subscribe("/topic/operation/" + documentId, new OperationFrameHandler(operationCallback));
        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error connecting to document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ----------------------------- USER UPDATE HANDLERS ----------------------------------------------- */
    public void sendUserUpdate(String documentId, User[] users) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: not connected");
            return;
        }
        try {
            // Trigger server-side operation login
            System.out.println("Sending user update: " + documentId + " " + Arrays.toString(users));
            stompSession.send("/app/users/" + documentId, users);
        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error connecting to document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void receiveUserUpdates(String documentId, Consumer<User[]> usersCallback) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: not connected");
            return;
        }
        try {
            // Trigger server-side operation login
            stompSession.subscribe("/response/users/" + documentId, new UserArrayFrameHandler(usersCallback));
        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error connecting to document: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void close() {
        if (this.stompSession != null) {
            try {
                System.out.println("WEBSOCKET: Disconnecting session");
                this.stompSession.disconnect();
                System.out.println("WEBSOCKET: Session disconnected");
            } catch (Exception e) {
                System.err.println("WEBSOCKET: Error disconnecting: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}