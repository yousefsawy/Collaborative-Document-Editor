package org.example.texteditor.WebSocketHandler.FrameHandlers;

import CRDT.Operation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class OperationFrameHandler implements StompFrameHandler {
    @Getter
    Operation operation;

    private final Consumer<Operation> onOperationReceived;

    public OperationFrameHandler(Consumer<Operation> onOperationReceived) {
        this.onOperationReceived = onOperationReceived;
    }

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
                if (onOperationReceived != null) {
                    onOperationReceived.accept(operation);
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
