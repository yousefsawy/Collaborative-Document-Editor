package CRDT;

import java.util.*;

public class CRDT_TREE {

    public String docName;
    public String userName;
    public Node root;
    public Map<ID, Node> idNodeMap;

    private Stack<Operation> undoStack = new Stack<>();
    private Stack<Operation> redoStack = new Stack<>();

    public CRDT_TREE(String docName, String userName) {
        ID owner = new ID(docName, 0);
        this.root = new Node(owner, "", null);
        idNodeMap = new HashMap<>();
        idNodeMap.put(owner, this.root);
        this.docName = docName;
        this.userName = userName;
    }

    public CRDT_TREE(String docName, String userName, Node[] nodes) {
        this.docName = docName;
        this.userName = userName;
        this.idNodeMap = new HashMap<>();

        // Handle empty array case
        if (nodes.length == 0) {
            ID rootId = new ID(docName, 0);
            this.root = new Node(rootId, "", null);
            idNodeMap.put(root.id, root);
            return;
        }

        // Create deep copies of all nodes to avoid reference issues
        for (Node node : nodes) {
            Node newNode = new Node(node.id, node.content, node.parentId);
            newNode.isDeleted = node.isDeleted;
            newNode.children = new ArrayList<>(); // Start with empty children list
            idNodeMap.put(newNode.id, newNode);
        }

        // Set the root
        this.root = idNodeMap.get(nodes[0].id);

        // Reconstruct parent-child relationships using a Set to avoid duplicates
        for (Node originalNode : nodes) {
            if (originalNode.parentId == null) {
                continue; // Skip root node
            }

            Node newChild = idNodeMap.get(originalNode.id);
            Node newParent = idNodeMap.get(originalNode.parentId);

            if (newParent != null && newChild != null) {
                // Use a Set operation to ensure uniqueness
                if (!newParent.children.contains(newChild.id)) {
                    newParent.children.add(newChild.id);
                }
            }
        }

        // Debug check: Verify no duplicate children in any node
        for (Node node : idNodeMap.values()) {
            Set<ID> uniqueChildren = new HashSet<>(node.children);
            if (uniqueChildren.size() < node.children.size()) {
                // There are duplicates in the children list
                node.children = new ArrayList<>(uniqueChildren); // Replace with deduplicated list
                System.out.println("Fixed duplicates in node: " + node.id);
            }
        }
    }


    //local ops
    private Node localInsertOne(int position, String text, long timeStamp) {
        String user = this.userName;
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
        return newNode;
    }

    // Insert multiple characters
    public Operation localInsert(int position, String text, long timeStamp) {
        String[] chars = splitToCharArray(text);

        if (chars.length == 0) {
            return null;
        }
        Node[] nodes = new Node[text.length()];

        // Insert the first character
        Node parent = localInsertOne(position, chars[0], timeStamp);
        nodes[0] = parent;
        // Insert the rest as children of the previous node
        for (int i = 1; i < chars.length; i++) {
            timeStamp++; // Increment timestamp for uniqueness
            position++;

            ID id = new ID(userName, timeStamp);
            Node newNode = new Node(id, chars[i], parent.id);

            parent.children.add(newNode.id);
            idNodeMap.put(id, newNode);

            nodes[i] = newNode;
            parent = newNode; // Chain the new node as the next parent
        }

        Operation op = new Operation(Operation.Type.INSERT, nodes, userName);

        undoStack.push(op);
        redoStack.clear(); // New operation invalidates redo history

        return op;
    }

    // Delete one character
    public Operation localDeleteOne(int position) {
        ID id = getParentByPosition(position);
        Node node = idNodeMap.get(id);
        node.isDeleted = true;

        Node[] nodes = new Node[]{node};

        Operation op = new Operation(Operation.Type.DELETE, nodes, userName);

        undoStack.push(op);
        redoStack.clear();

        return op;
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
    }

    public void remoteDelete(ID id) {
        if (!idNodeMap.containsKey(id)) {
            throw new IllegalArgumentException("Node not found for remote delete: " + id);
        }
        Node node = idNodeMap.get(id);
        node.isDeleted = true;
    }

    public void remoteUpdate(Operation op) {
        if (op == null || op.nodes == null || op.nodes.length == 0) {
            return;
        }

        switch (op.type) {
            case INSERT:
                for (Node opNode : op.nodes) {
                    if (idNodeMap.containsKey(opNode.id)) {
                        // Node exists, undelete it
                        Node localNode = idNodeMap.get(opNode.id);
                        localNode.isDeleted = false;
                    } else {
                        // Node doesn't exist, create it
                        remoteInsert(opNode.parentId, opNode.content, opNode.id);
                    }
                }
                break;

            case DELETE:
                for (Node opNode : op.nodes) {
                    Node localNode = idNodeMap.get(opNode.id);
                    if (localNode != null) {
                        localNode.isDeleted = true;
                    } else {
                        System.err.println("Tried to delete missing node: " + opNode.id);
                    }
                }
                break;

            case UNDO:
                // For UNDO operations, the effect depends on the original operation type
                for (Node opNode : op.nodes) {
                    Node localNode = idNodeMap.get(opNode.id);
                    if (localNode != null) {
                        // Toggle deletion state
                        localNode.isDeleted = !localNode.isDeleted;
                    } else {

                        System.err.println("Tried to update missing node: " + opNode.id);
                    }
                }
                break;

            case REDO:
                // For REDO operations, similar to UNDO
                for (Node opNode : op.nodes) {
                    Node localNode = idNodeMap.get(opNode.id);
                    if (localNode != null) {
                        // Toggle deletion state
                        localNode.isDeleted = !localNode.isDeleted;
                    } else {
                        // If node doesn't exist, recreate it
                        System.err.println("Tried to update missing node: " + opNode.id);
                    }
                }
                break;
        }
    }


    public Operation undo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        Operation op = undoStack.pop();
        Node[] nodes = op.nodes;


        if (op.type == Operation.Type.INSERT) {
            // Undo insert → delete the nodes
            for (Node node : nodes) {
                Node localNode = idNodeMap.get(node.id);
                if (localNode != null) {
                    localNode.isDeleted = true;
                }
            }
            // Create an UNDO operation for remote propagation
            Operation undoOp = new Operation(Operation.Type.UNDO, nodes, userName);

            // For local redo, use a DELETE operation
            Operation redoOp = new Operation(Operation.Type.INSERT, nodes, userName);
            redoStack.push(redoOp);

            return undoOp;
        } else if (op.type == Operation.Type.DELETE) {
            // Undo delete → undelete the nodes
            for (Node node : nodes) {
                Node localNode = idNodeMap.get(node.id);
                if (localNode != null) {
                    localNode.isDeleted = false;
                }
            }
            // Create an UNDO operation for remote propagation
            Operation undoOp = new Operation(Operation.Type.UNDO, nodes, userName);
            Operation redoOp = new Operation(Operation.Type.DELETE, nodes, userName);
            // For local redo, use an INSERT operation

            redoStack.push(redoOp);

            return undoOp;
        }

        return null;
    }

    public Operation redo() {
        if (redoStack.isEmpty()) {
            return null;
        }

        Operation op = redoStack.pop();
        Node[] nodes = op.nodes;

        if (op.type == Operation.Type.INSERT) {
            // Redo insert → undelete nodes
            for (Node node : nodes) {
                Node localNode = idNodeMap.get(node.id);
                if (localNode != null) {
                    localNode.isDeleted = false;
                }
            }
            // Create a REDO operation for remote propagation
            Operation redoOp = new Operation(Operation.Type.REDO, nodes, userName);

            // For local undo, use a DELETE operation
            Operation undoOp = new Operation(Operation.Type.INSERT, nodes, userName);
            undoStack.push(undoOp);

            return redoOp;
        } else if (op.type == Operation.Type.DELETE) {
            // Redo delete → delete nodes
            for (Node node : nodes) {
                Node localNode = idNodeMap.get(node.id);
                if (localNode != null) {
                    localNode.isDeleted = true;
                }
            }
            // Create a REDO operation for remote propagation
            Operation redoOp = new Operation(Operation.Type.REDO, nodes, userName);

            // For local undo, use an INSERT operation
            Operation undoOp = new Operation(Operation.Type.DELETE, nodes, userName);
            undoStack.push(undoOp);

            return redoOp;
        }

        return null;
    }


    public Node[] sendTree() {
        if (root == null) {
            return new Node[0];
        }

        ArrayList<Node> result = new ArrayList<>();

        // Use a Set to track which nodes we've already visited by ID
        Set<ID> visitedIds = new HashSet<>();

        // Queue for BFS
        ArrayList<Node> queue = new ArrayList<>();
        queue.add(root);
        visitedIds.add(root.id);

        // BFS traversal
        while (!queue.isEmpty()) {
            Node current = queue.remove(0);

            // Add current node to result
            result.add(current);

            // Add all children to the queue (if not already visited)
            for (ID childId : current.children) {
                if (!visitedIds.contains(childId)) {
                    Node childNode = getNodeById(childId);
                    if (childNode != null) {
                        queue.add(childNode);
                        visitedIds.add(childId);
                    }
                }
            }
        }

        // Convert ArrayList to array
        return result.toArray(new Node[0]);
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


}


