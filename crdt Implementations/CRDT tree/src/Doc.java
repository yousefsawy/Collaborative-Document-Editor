
import java.util.HashMap;
import java.util.Map;

public class Doc {

    private Item root; // Root of the tree
    private Map<String, Long> version; // Track latest timestamp per agent
    private Map<String, Item> itemsById; // For quick lookups by ID

    public Doc() {
        // Create a special root node with null ID and content
        root = new Item("", null, null);
        version = new HashMap<>();
        itemsById = new HashMap<>();
    }

    /**
     * Gets the document content by traversing the tree in-order
     */
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        collectContent(root, sb);
        return sb.toString();
    }

    private void collectContent(Item node, StringBuilder sb) {
        if (node != root && !node.deleted) {
            sb.append(node.getContent());
        }

        for (Item child : node.children) {
            collectContent(child, sb);
        }
    }

    /**
     * Insert a single character at position with specific agent
     */
    public void localInsertOne(int position, String text, String agent) {
        // Update agent's timestamp
        long timestamp = System.currentTimeMillis();
        if (version.containsKey(agent)) {
            timestamp = Math.max(timestamp, version.get(agent) + 1);
        }
        version.put(agent, timestamp);

        // Find parent node where we should insert
        Item parentNode = findNodeAtPosition(position);

        // Create new item
        ID newId = new ID(agent, timestamp);
        Item newItem = new Item(text, newId, parentNode.id);

        // Add to parent's children at appropriate position
        int posInParent = getPositionInParent(position, parentNode);
        parentNode.children.add(posInParent, newItem);

        // Store in lookup map
        itemsById.put(idToString(newId), newItem);
    }

    /**
     * Insert text at position with specific agent
     */
    public void localInsert(int position, String text, String agent) {
        String[] chars = splitToCharArray(text);
        for (int i = 0; i < chars.length; i++) {
            localInsertOne(position, chars[i], agent);
            position++;
        }
    }

    /**
     * Process a remote insert from another site
     */
    public void remoteInsert(Item newItem) {
        // Find the parent node
        Item parent = null;
        if (newItem.getParent() == null) {
            parent = root;
        } else {
            parent = itemsById.get(idToString(newItem.getParent()));
            if (parent == null) {
                // Parent not found, use root as fallback
                parent = root;
            }
        }

        // Determine position among siblings (by timestamp)
        int insertPos = 0;
        while (insertPos < parent.children.size()
                && compareIds(parent.children.get(insertPos).getId(), newItem.getId()) < 0) {
            insertPos++;
        }

        // Add to parent's children
        parent.children.add(insertPos, newItem);

        // Store in lookup map
        itemsById.put(idToString(newItem.getId()), newItem);

        // Update version vector if needed
        String agent = newItem.getId().getAgent();
        long timestamp = newItem.getId().getTimestamp();
        if (!version.containsKey(agent) || version.get(agent) < timestamp) {
            version.put(agent, timestamp);
        }
    }

    /**
     * Delete a character at the specified position
     */
    public void localDelete(int position) {
        Item item = findItemAtPosition(position);
        if (item != null && item != root) {
            item.deleted = true;
        }
    }

    /**
     * Find the node that contains the position
     */
    private Item findNodeAtPosition(int position) {
        if (position <= 0) {
            return root;
        }

        int currentPos = 0;
        return findNodeAtPositionRecursive(root, position, currentPos);
    }

    private Item findNodeAtPositionRecursive(Item node, int targetPos, int currentPos) {
        // For each child
        for (Item child : node.children) {
            if (!child.deleted) {
                currentPos++; // The position of this node

                // If this is our target position, return the parent
                if (currentPos == targetPos) {
                    return node;
                }

                // Check if position is within this subtree
                int childrenPos = countChildren(child);
                if (currentPos + childrenPos >= targetPos) {
                    return findNodeAtPositionRecursive(child, targetPos, currentPos);
                }

                // Skip past this subtree
                currentPos += childrenPos;
            }
        }

        return node; // Default to returning the node itself
    }

    /**
     * Find the exact item at the position
     */
    private Item findItemAtPosition(int position) {
        if (position < 0 || root.children.isEmpty()) {
            return null;
        }

        int currentPos = 0;
        return findItemAtPositionRecursive(root, position, currentPos);
    }

    private Item findItemAtPositionRecursive(Item node, int targetPos, int currentPos) {
        for (Item child : node.children) {
            if (!child.deleted) {
                // Is this the item at the target position?
                if (currentPos == targetPos) {
                    return child;
                }
                currentPos++;

                // Check child's subtree
                int childrenCount = countChildren(child);
                if (currentPos + childrenCount > targetPos) {
                    return findItemAtPositionRecursive(child, targetPos, currentPos);
                }

                currentPos += childrenCount;
            }
        }
        return null;
    }

    /**
     * Calculate position within parent's children
     */
    private int getPositionInParent(int globalPosition, Item parent) {
        // Count visible children and determine insert position
        int count = 0;
        int pos = 0;

        while (count < parent.children.size()) {
            if (globalPosition <= pos) {
                return count;
            }
            if (!parent.children.get(count).deleted) {
                pos++;
            }
            count++;
        }

        return parent.children.size();
    }

    /**
     * Count visible children in a subtree
     */
    private int countChildren(Item node) {
        int count = 0;
        for (Item child : node.children) {
            if (!child.deleted) {
                count++; // Count the child
                count += countChildren(child); // Count its descendants
            }
        }
        return count;
    }

    /**
     * Compare two IDs (timestamp first, then agent)
     */
    private int compareIds(ID a, ID b) {
        if (a.getTimestamp() != b.getTimestamp()) {
            return Long.compare(a.getTimestamp(), b.getTimestamp());
        }
        return a.getAgent().compareTo(b.getAgent());
    }

    /**
     * Convert ID to string for map key
     */
    private String idToString(ID id) {
        if (id == null) {
            return "null";
        }
        return id.getAgent() + "-" + id.getTimestamp();
    }

    /**
     * Split text into character array
     */
    public String[] splitToCharArray(String input) {
        String[] result = new String[input.length()];
        for (int i = 0; i < input.length(); i++) {
            result[i] = String.valueOf(input.charAt(i));
        }
        return result;
    }

    /**
     * Print the tree structure for debugging
     */
    public void printDoc() {
        System.out.println("Document Tree:");
        printNode(root, 0);
    }

    private void printNode(Item node, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }

        String nodeInfo = node == root ? "ROOT"
                : String.format("%s (ID: %s, Deleted: %s)",
                        node.getContent(),
                        node.getId(),
                        node.deleted);

        System.out.println(indent + nodeInfo);

        for (Item child : node.children) {
            printNode(child, depth + 1);
        }
    }
}
