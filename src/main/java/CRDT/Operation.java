package CRDT;

public class Operation {

    public enum Type {
        INSERT, DELETE
    }

    public Type type;
    ID[] ids;

    public Operation(Type type, ID[] ids) {
        this.type = type;
        this.ids = ids;
    }
}
