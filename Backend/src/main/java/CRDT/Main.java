package CRDT;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        CRDT_TREE doc1 = new CRDT_TREE("1");
        CRDT_TREE doc2 = new CRDT_TREE("2");

        ID[] ids = doc1.localInsert(0, "omar", 100);
        Node[] nodes = new Node[ids.length];
        for (int i = 0; i < ids.length; i++) {
            nodes[i] = doc1.getNodeById(ids[i]);
            doc2.remoteInsert(nodes[i].parentId, nodes[i].content, nodes[i].id);
        }

        doc1.printTree();
        doc2.printTree();
        System.out.println(doc1.getDocument());
    }
}
