package org.example.texteditor.WebSocketHandler;

import CRDT.Node;
import CRDT.Operation;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class WebSocketHandler {
    private StompSession stompSession;
    Node[] nodes;
    Operation operation;
    private Consumer<Node[]> onNodesReceived;


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

    // Custom STOMP handler specifically for Node array messages
    private class NodeArrayFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            // This is critical - tell the converter we're expecting a Node array
            return Node[].class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                if (payload instanceof Node[]) {
                    nodes = (Node[]) payload;
                    System.out.println("WEBSOCKET: Received " + nodes.length + " nodes from server");
                    if (onNodesReceived != null) {
                        onNodesReceived.accept(nodes);
                    }

//                    // Log some details about the nodes
//                    for (int i = 0; i < Math.min(nodes.length, 3); i++) {
//                        Node node = nodes[i];
//                        System.out.println("Node " + i + ": ID=" + node.getId() + ", Content=" + node.getContent());
//                    }
//                    if (nodes.length > 3) {
//                        System.out.println("... and " + (nodes.length - 3) + " more nodes");
//                    }
                } else {
                    System.err.println("WEBSOCKET: Received non-Node array payload: " + payload.getClass());
                }
            } catch (Exception e) {
                System.err.println("WEBSOCKET: Error processing node array: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Custom STOMP handler specifically for Node array messages
    private class OperationHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Operation.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                if (payload instanceof Operation) {
                    operation = (Operation) payload;
                    System.out.println("WEBSOCKET: Received Operation from server");

                } else {
                    System.err.println("WEBSOCKET: Received non-Node array payload: " + payload.getClass());
                }
            } catch (Exception e) {
                System.err.println("WEBSOCKET: Error processing node array: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public Node[] getNodes() {
        return nodes;
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

    public void sendDocumentOperation(String message, String documentId) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: Cannot send message - not connected!");
            return;
        }

        try {
            System.out.println("WEBSOCKET: Sending operation to document " + documentId);

            // Send message to server
            stompSession.send("/app/connect/" + documentId, message);
            System.out.println("WEBSOCKET: Message sent successfully");

        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void connectToDocument(String documentId) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: Cannot subscribe - not connected");
            return;
        }

        try {
            System.out.println("========================================");
            System.out.println("WEBSOCKET: Subscribing to document: " + documentId);

            // Use our dedicated handler for Node arrays
            NodeArrayFrameHandler frameHandler = new NodeArrayFrameHandler();

            // Subscribe to the topic with our Node-specific handler
            StompSession.Subscription subscription = stompSession.subscribe(
                    "/user/response/connect", frameHandler);

            System.out.println("WEBSOCKET: Subscription ID: " + subscription.getSubscriptionId());

            stompSession.send("/app/connect/" + documentId, "");

        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error connecting to document: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void connectToDocumentAsync(String documentId, Consumer<Node[]> callback) {
        if (stompSession == null || !stompSession.isConnected()) {
            System.err.println("WEBSOCKET: Cannot subscribe - not connected");
            return;
        }

        try {
            System.out.println("========================================");
            System.out.println("WEBSOCKET: Subscribing to document: " + documentId);

            // Store the callback
            this.onNodesReceived = callback;

            NodeArrayFrameHandler frameHandler = new NodeArrayFrameHandler();

            stompSession.subscribe("/user/response/connect", frameHandler);

            // Trigger server-side connect logic
            stompSession.send("/app/connect/" + documentId, "");

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