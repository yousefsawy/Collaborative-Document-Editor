public class Main {
    public static void main(String[] args) throws InterruptedException {
        CRDT_TREE doc1 = new CRDT_TREE("1");


        doc1.localInsert(0,"omar",100);
        doc1.undo();
        doc1.redo();
        doc1.localInsert(0,"ali",200);

        doc1.printTree();
        System.out.println(doc1.getDocument());

    }
}
