package org.example.texteditor.WebSocketHandler.FrameHandlers;

import CRDT.Node;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class NodeArrayFrameHandler implements StompFrameHandler {
    final private Consumer<Node[]> onNodesReceived;

    @Getter
    Node[] nodes;

    public NodeArrayFrameHandler(Consumer<Node[]> onNodesReceived) {
        this.onNodesReceived = onNodesReceived;
    }

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
            } else {
                System.err.println("WEBSOCKET: Received non-Node array payload: " + payload.getClass());
            }
        } catch (Exception e) {
            System.err.println("WEBSOCKET: Error processing node array: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
