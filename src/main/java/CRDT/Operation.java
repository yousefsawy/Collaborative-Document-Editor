package CRDT;

public class Operation {

    public enum Type {
        INSERT, DELETE
    }

    public Type type;
    Node[] nodes;

    public Operation(Type type, Node[] nodes) {
        this.type = type;
        this.nodes = nodes;
    }
}
