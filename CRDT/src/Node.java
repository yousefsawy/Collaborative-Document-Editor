import java.util.ArrayList;

public class Node {
    public ID id;
    public String content;
    public boolean isDeleted;
    public ID parentId;
    public ArrayList<ID> children;

    public Node(ID id, String content, ID parentId) {
        this.id = id;
        this.content = content;
        this.parentId = parentId;
        this.isDeleted = false;
        children = new ArrayList<>();
    }
}
