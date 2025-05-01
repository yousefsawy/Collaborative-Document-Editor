package org.example.texteditor.WebSocketHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.converter.MessageConverter;

public class WebSocketHandler extends StompSessionHandlerAdapter {
    StompSession stompSession;
    public void connectToWebSocket() {
        try {
            System.out.println("Websocket trying to connect to websocket");

            // Step 1: Setup SockJS transports
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            // Step 2: Use SockJS client in your Stomp client
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

            // Step 3: Set a message converter (JSON to object)
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            // Step 4: Connect to the server
            String url = "ws://localhost:8080/ws";  // Note: SockJS will automatically adjust internally
            this.stompSession = stompClient
                    .connectAsync(url, new MyStompSessionHandler())
                    .get();

            if (this.stompSession.isConnected()) {
                System.out.println("WebSocket is connected");
            } else {
                System.out.println("WebSocket is not connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDocumentOperation(String option,String documentId) {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.send("/app/operation/" + documentId, option);
            System.out.println("Sent vote: " + option + " to document " + documentId);
        } else {
            System.out.println("Failed to send vote. WebSocket session is not connected.");
        }
    }



    //    public void subscribeToDocument(String documentId){
//        if (stompSession != null && stompSession.isConnected()) {
//            // Subscribe to the poll result topic
//            stompSession.subscribe("/topic/pollResult/" + documentId, new StompFrameHandler() {
//
//                @Override
//                public Type getPayloadType(StompHeaders headers) {
//                    return PollResult.class;  // Expected response type is PollResult
//                }
//
//                @Override
//                public void handleFrame(StompHeaders headers, Object payload) {
//                    // Handle the incoming poll result message
//                    PollResult pollResult = (PollResult) payload;
//                    printPollResult(pollResult);
//                }
//            });
//            System.out.println("Subscribed to poll results for poll ID: " + documentId);
//        } else {
//            System.out.println("Failed to subscribe. WebSocket session is not connected.");
//        }
//
//    }
    public void close(){
        this.stompSession.disconnect();
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {

    }

}


