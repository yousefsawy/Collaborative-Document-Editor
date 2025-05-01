package CRDT;

public class Operation {

    public enum Type {
        INSERT, DELETE, UNDO, REDO
    }

    public Type type;
    ID[] ids;
    string userId;

    public Operation(Type type, ID[] ids) {
        this.type = type;
        this.ids = ids;
    }
}
