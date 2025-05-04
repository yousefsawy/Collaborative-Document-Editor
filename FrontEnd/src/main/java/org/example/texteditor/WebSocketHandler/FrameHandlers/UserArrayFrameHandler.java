package org.example.texteditor.WebSocketHandler.FrameHandlers;

import CRDT.Node;
import lombok.Getter;
import lombok.Setter;
import org.example.texteditor.DTO.User;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class UserArrayFrameHandler implements StompFrameHandler {
    private Consumer<String[]> onUsersReceiverd;

    @Getter
    String[] users;

    public UserArrayFrameHandler(Consumer<String[]> onUsersReceived) {
        this.onUsersReceiverd = onUsersReceived;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String[].class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            if (payload instanceof String[]) {
                users = (String[]) payload;
                System.out.println("WEBSOCKET: Received " + users.length + " users from server");
                if (onUsersReceiverd != null) {
                    onUsersReceiverd.accept(users);
                }
            } else {
                System.err.println("WEBSOCKET: Received non-Node array payload: " + payload.getClass());
            }
        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error processing node array: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
