
import java.util.ArrayList;
import java.util.List;

public class Item {

    public String content;
    public ID id;
    public ID parent; // Renamed from originLeft
    public boolean deleted = false;

    public List<Item> children = new ArrayList<>();

    public Item(String content, ID id, ID parent) { // Updated constructor
        this.content = content;
        this.id = id;
        this.parent = parent; // Renamed from originLeft
    }

    public String getContent() {
        return content;
    }

    public ID getId() {
        return id;
    }

    public ID getParent() {
        return parent; // Renamed from getOriginLeft
    }
}
