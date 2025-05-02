package CRDT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class Node implements Serializable {

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
