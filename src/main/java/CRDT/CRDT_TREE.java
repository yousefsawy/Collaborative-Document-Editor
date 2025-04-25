package CRDT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CRDT_TREE {

    public String name;
    public Node root;
    private Map<ID, Node> idNodeMap;
    private ArrayList<Node> nodeList = new ArrayList<>();

    private Stack<Operation> undoStack = new Stack<>();
    private Stack<Operation> redoStack = new Stack<>();

    public CRDT_TREE(String name) {
        ID owner = new ID("allFather", 0);
        this.root = new Node(owner, "", null);
        idNodeMap = new HashMap<>();
        idNodeMap.put(owner, this.root);
        this.name = name;
    }

    //local ops
    private Node localInsertOne(int position, String text, long timeStamp) {
        String user = this.name;
        ID id = new ID(user, timeStamp);
        Node parent;
        if (position == 0) {
            parent = root;
        } else {
            ID parentId = getParentByPosition(position);
            parent = idNodeMap.get(parentId);
        }
        Node newNode = new Node(id, text, parent.id);
        parent.children.add(newNode.id);
        idNodeMap.put(id, newNode);
        nodeList.add(position, newNode);
        return newNode;
    }

    // Insert multiple characters
    public ID[] localInsert(int position, String text, long timeStamp) {
        String user = this.name;
        String[] chars = splitToCharArray(text);
        if (chars.length == 0) {
            return null;
        }

        ID[] insertedIds = new ID[chars.length];

        // Insert the first character
        Node parent = localInsertOne(position, chars[0], timeStamp);
        insertedIds[0] = parent.id;

        // Insert the rest as children of the previous node
        for (int i = 1; i < chars.length; i++) {
            timeStamp++; // Increment timestamp for uniqueness
            position++;

            ID id = new ID(user, timeStamp);
            Node newNode = new Node(id, chars[i], parent.id);

            parent.children.add(newNode.id);
            idNodeMap.put(id, newNode);
            nodeList.add(position, newNode);

            insertedIds[i] = id;
            parent = newNode; // Chain the new node as the next parent
        }

        undoStack.push(new Operation(Operation.Type.DELETE, insertedIds));
        redoStack.clear(); // New operation invalidates redo history

        return insertedIds;
    }

    // Delete one character
    public ID[] localDeleteOne(int position) {
        ID id = getParentByPosition(position);
        Node node = idNodeMap.get(id);
        node.isDeleted = true;

        ID[] deletedIds = new ID[]{id};

        undoStack.push(new Operation(Operation.Type.INSERT, deletedIds));
        redoStack.clear();

        return deletedIds;
    }

    // remote ops
    public void remoteInsert(ID parentId, String text, ID id) {
        if (idNodeMap.containsKey(id)) {
            return;
        }
        Node parent = idNodeMap.get(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("Parent not found for remote insert: " + parentId);
        }

        // 3. Create and link the new node
        Node newNode = new Node(id, text, parentId);
        parent.children.add(newNode.id);
        idNodeMap.put(id, newNode);
        nodeList.add(newNode);
    }

    public void remoteDelete(ID id) {
        if (!idNodeMap.containsKey(id)) {
            throw new IllegalArgumentException("Node not found for remote delete: " + id);
        }
        Node node = idNodeMap.get(id);
        node.isDeleted = true;
    }

    public void remoteUpdate(Operation op) {
        if (op == null || op.ids == null) {
            return;
        }

        if (op.type == Operation.Type.INSERT) {
            for (ID id : op.ids) {
                if (idNodeMap.containsKey(id)) {
                    Node node = idNodeMap.get(id);
                    node.isDeleted = false; // In case it was deleted
                } else {
                    System.err.println("Missing node for remote insert: " + id);
                    // If needed: you could buffer missing operations for retry
                }
            }
        } else if (op.type == Operation.Type.DELETE) {
            for (ID id : op.ids) {
                Node node = idNodeMap.get(id);
                if (node != null) {
                    node.isDeleted = true;
                } else {
                    System.err.println("Tried to delete missing node: " + id);
                }
            }
        }
    }

    public Operation undo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        Operation op = undoStack.pop();

        if (op.type == Operation.Type.INSERT) {
            // Undo insert → delete the nodes
            for (ID id : op.ids) {
                Node node = idNodeMap.get(id);
                if (node != null) {
                    node.isDeleted = true;
                }
            }
            Operation reverse = new Operation(Operation.Type.DELETE, op.ids);
            redoStack.push(reverse);
            return reverse;
        } else if (op.type == Operation.Type.DELETE) {
            // Undo delete → undelete the nodes
            for (ID id : op.ids) {
                Node node = idNodeMap.get(id);
                if (node != null) {
                    node.isDeleted = false;
                }
            }
            Operation reverse = new Operation(Operation.Type.INSERT, op.ids);
            redoStack.push(reverse);
            return reverse;
        }

        return null;
    }

    public Operation redo() {
        if (redoStack.isEmpty()) {
            return null;
        }

        Operation op = redoStack.pop();

        if (op.type == Operation.Type.INSERT) {
            // Redo insert → undelete nodes
            for (ID id : op.ids) {
                Node node = idNodeMap.get(id);
                if (node != null) {
                    node.isDeleted = false;
                }
            }
            Operation reverse = new Operation(Operation.Type.DELETE, op.ids);
            undoStack.push(reverse);
            return reverse;
        } else if (op.type == Operation.Type.DELETE) {
            // Redo delete → delete nodes
            for (ID id : op.ids) {
                Node node = idNodeMap.get(id);
                if (node != null) {
                    node.isDeleted = true;
                }
            }
            Operation reverse = new Operation(Operation.Type.INSERT, op.ids);
            undoStack.push(reverse);
            return reverse;
        }

        return null;
    }

    // helper funcitons
    public Node getNodeById(ID id) {
        return idNodeMap.get(id);
    }

    private String[] splitToCharArray(String input) {
        String[] result = new String[input.length()];
        for (int i = 0; i < input.length(); i++) {
            result[i] = String.valueOf(input.charAt(i));
        }
        return result;
    }

    private ID getParentByPosition(Node node, int position, Counter counter) {
        if (node == null) {
            return null;
        }

        if (!node.isDeleted) {
            counter.value++;
            if (counter.value - 1 == position) {
                return node.id;
            }
        }

        node.children.sort((a, b) -> {
            if (b.timeStamp != a.timeStamp) {
                return Long.compare(b.timeStamp, a.timeStamp); // Descending time
            }
            return a.user.compareTo(b.user); // Ascending user
        });

        for (ID childID : node.children) {
            Node child = idNodeMap.get(childID);
            ID result = getParentByPosition(child, position, counter);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private ID getParentByPosition(int position) {
        return getParentByPosition(root, position, new Counter());
    }

    // Simple counter class
    private static class Counter {

        int value = 0;
    }

    // printing
    public void printTree() {
        System.out.printf("%-20s %-10s %-10s %-20s %-30s\n", "ID (user,ts)", "Content", "Deleted", "Parent ID", "Children IDs");
        System.out.println("-----------------------------------------------------------------------------------------------------------");
        printTree(root);
    }

    private void printTree(Node node) {
        if (node == null) {
            return;
        }

        String idStr = "(" + node.id.user + "," + node.id.timeStamp + ")";
        String content = node.content;
        String isDeleted = node.isDeleted ? "Yes" : "No";

        String parentStr = (node.parentId == null) ? "null"
                : "(" + node.parentId.user + "," + node.parentId.timeStamp + ")";

        StringBuilder childrenStr = new StringBuilder();
        for (ID childID : node.children) {
            childrenStr.append("(").append(childID.user).append(",").append(childID.timeStamp).append(") ");
        }

        System.out.printf("%-20s %-10s %-10s %-20s %-30s\n",
                idStr, content, isDeleted, parentStr, childrenStr.toString().trim());

        for (ID childID : node.children) {
            Node child = idNodeMap.get(childID);
            printTree(child);
        }
    }

    private String getDocument(Node node) {
        if (node == null) {
            return "";
        }

        // Start with the current node's content
        StringBuilder sb = new StringBuilder();
        if (!node.isDeleted) {
            sb.append(node.content);
        }

        // Sort children as specified
        ArrayList<ID> children = node.children;
        children.sort((a, b) -> {
            if (b.timeStamp != a.timeStamp) {
                return Long.compare(b.timeStamp, a.timeStamp); // descending timestamp
            }
            return a.user.compareTo(b.user); // ascending user
        });

        // Recursively get content from children
        for (ID childID : children) {
            Node child = idNodeMap.get(childID);
            sb.append(getDocument(child)); // depth-first traversal
        }

        return sb.toString();
    }

    // Public function that starts from root
    public String getDocument() {
        return getDocument(root);
    }

    public void printList() {
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            System.out.println(i + " " + node.content + " " + node.isDeleted);
        }
    }

}
