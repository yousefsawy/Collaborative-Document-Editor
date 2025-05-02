package CRDT;


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

}
