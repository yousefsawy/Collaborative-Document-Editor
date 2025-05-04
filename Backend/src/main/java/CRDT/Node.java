package CRDT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Node {

    public ID id;
    public String content;
    public boolean isDeleted;
    public ID parentId;

    @JsonIgnore
    public ArrayList<ID> children;

    public Node(ID id, String content, ID parentId) {
        this.id = id;
        this.content = content;
        this.parentId = parentId;
        this.isDeleted = false;
        children = new ArrayList<>();
    }
    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", isDeleted=" + isDeleted +
                ", parentId=" + parentId +
                ", children=" + children +
                '}';
    }


}
