package CRDT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
public class Operation {

    public enum Type {
        INSERT, DELETE,UNDO,REDO
    }

    public Type type;
    public Node[] nodes;
    public String user;


    public Operation(Type type, Node[] nodes, String user) {
        this.type = type;
        this.nodes = nodes;
        this.user = user;
    }

    public void print() {
        System.out.println("Operation:");
        System.out.println("  Type: " + type);
        System.out.println("  User: " + user);
        System.out.println("  Nodes: " + Arrays.toString(nodes));
    }


}
