
public class CRDT {

    private Item root; // Root of the tree (the document)

    public CRDT() {
        this.root = new Item("", new ID("root", 0), null); // Root item with no content
    }

    // Insert a new item into the CRDT tree
    public void insertItem(String content, String agent, long timestamp, ID parentId) {
        ID newItemId = new ID(agent, timestamp);
        Item newItem = new Item(content, newItemId, parentId);

        if (parentId == null) {
            // If no parent is given, insert at root
            root.children.add(newItem);
        } else {
            // Otherwise, find the parent and insert the item as a child
            Item parentItem = findItemById(root, parentId);
            if (parentItem != null) {
                parentItem.children.add(newItem);
            } else {
                System.out.println("Parent not found!");
            }
        }
    }

    // Find an item by its ID, starting from the root
    private Item findItemById(Item currentItem, ID itemId) {
        if (currentItem.id.equals(itemId)) {
            return currentItem;
        }

        // Recursively check each child
        for (Item child : currentItem.children) {
            Item found = findItemById(child, itemId);
            if (found != null) {
                return found;
            }
        }
        return null; // If item is not found
    }

    // Traverse the CRDT tree and print its contents
    public void printTree() {
        printTreeRecursive(root, 0);
    }

    // Helper recursive method to print the tree
    private void printTreeRecursive(Item currentItem, int depth) {
        // Print current item
        System.out.println("  ".repeat(depth) + "Content: '" + currentItem.getContent() + "' | ID: " + currentItem.getId());

        // Recursively print all children
        for (Item child : currentItem.children) {
            printTreeRecursive(child, depth + 1);
        }
    }

}
