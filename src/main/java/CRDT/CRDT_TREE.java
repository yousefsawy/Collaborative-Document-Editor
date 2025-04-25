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
        parent.children.add(newNode.id);;
        idNodeMap.put(id, newNode);
        nodeList.add(position, newNode);
        return newNode;
    }

    public Node[] localInsert(int position, String text, long timeStamp) {
        String user = this.name;
        String[] chars = splitToCharArray(text);
        if (chars.length == 0) {
            return null;
        }
        Node[] nodes = new Node[chars.length];

        // Insert the first character
        Node parent = localInsertOne(position, chars[0], timeStamp);
        nodes[0] = parent;
        // Insert the rest as children of the previous node
        for (int i = 1; i < chars.length; i++) {
            timeStamp++; // Increment timestamp for uniqueness
            position++;
            ID id = new ID(user, timeStamp);
            Node newNode = new Node(id, chars[i], parent.id);
            nodes[i] = newNode;
            parent.children.add(newNode.id);
            idNodeMap.put(id, newNode);
            nodeList.add(position, newNode);
            parent = newNode; // Chain the new node as the next parent
        }

        undoStack.push(new Operation(Operation.Type.DELETE, nodes));
        redoStack.clear(); // New operation invalidates redo history
        return nodes;
    }

    public Node[] localDeleteOne(int position) {
        ID id = getParentByPosition(position);
        Node node = idNodeMap.get(id);
        node.isDeleted = true;
        Node[] nodes = new Node[1];
        nodes[0] = node;
        undoStack.push(new Operation(Operation.Type.INSERT, nodes));
        redoStack.clear();
        return nodes;
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
        if (op == null || op.nodes == null) {
            return;
        }

        if (op.type == Operation.Type.INSERT) {
            for (Node node : op.nodes) {
                if (idNodeMap.containsKey(node.id)) {
                    continue; // Already inserted
                }
                Node parent = idNodeMap.get(node.parentId);
                if (parent == null) {
                    System.err.println("Missing parent for remote insert: " + node.parentId);
                    continue; // Optionally: queue it for retry
                }

                // Add node
                parent.children.add(node.id);
                idNodeMap.put(node.id, node);
                nodeList.add(node); // Optional: preserve linear history
            }
        } else if (op.type == Operation.Type.DELETE) {
            for (Node node : op.nodes) {
                Node existing = idNodeMap.get(node.id);
                if (existing != null) {
                    existing.isDeleted = true;
                } else {
                    System.err.println("Tried to delete missing node: " + node.id);
                }
            }
        }
    }

    //undo and redo
    public Node[] undo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        Operation op = undoStack.pop();
        Node[] affectedNodes = new Node[op.nodes.length];

        if (op.type == Operation.Type.INSERT) {
            // Redo insert means re-inserting previously deleted nodes
            for (int i = 0; i < op.nodes.length; i++) {
                Node node = op.nodes[i];
                node.isDeleted = false;
                affectedNodes[i] = node;
            }
            redoStack.push(new Operation(Operation.Type.DELETE, op.nodes));
        } else if (op.type == Operation.Type.DELETE) {
            // Redo delete means marking inserted nodes as deleted again
            for (int i = 0; i < op.nodes.length; i++) {
                Node node = op.nodes[i];
                node.isDeleted = true;
                affectedNodes[i] = node;
            }
            redoStack.push(new Operation(Operation.Type.INSERT, op.nodes));
        }

        return affectedNodes;
    }

    public Node[] redo() {
        if (redoStack.isEmpty()) {
            return null;
        }

        Operation op = redoStack.pop();
        Node[] affectedNodes = new Node[op.nodes.length];

        if (op.type == Operation.Type.INSERT) {
            for (int i = 0; i < op.nodes.length; i++) {
                Node node = op.nodes[i];
                node.isDeleted = false;
                affectedNodes[i] = node;
            }
            undoStack.push(new Operation(Operation.Type.DELETE, op.nodes));
        } else if (op.type == Operation.Type.DELETE) {
            for (int i = 0; i < op.nodes.length; i++) {
                Node node = op.nodes[i];
                node.isDeleted = true;
                affectedNodes[i] = node;
            }
            undoStack.push(new Operation(Operation.Type.INSERT, op.nodes));
        }

        return affectedNodes;
    }

    // helper funcitons
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
