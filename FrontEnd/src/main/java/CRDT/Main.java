package CRDT;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        CRDT_TREE doc1 = new CRDT_TREE("1","omar");
        doc1.localInsert(0, "a", 100);
        doc1.localInsert(1, "b", 101);
        doc1.localInsert(2, "c", 102);
        doc1.localDeleteOne(3);
        doc1.localDeleteOne(2);
        doc1.localDeleteOne(1);

        doc1.localInsert(0, "a", 103);
        doc1.localInsert(1, "b", 104);
        doc1.localInsert(2, "c", 105);











//        CRDT_TREE doc2 = new CRDT_TREE("1","anas");
//
//
//        Operation op1 = doc1.localInsert(0, "omar", 100);
//
//        doc2.remoteUpdate(op1);
//
//        Operation op2 = doc1.undo();
//        doc2.remoteUpdate(op2);
//
//
//        Operation op3 = doc1.redo();
//        doc2.remoteUpdate(op3);
//
//        Operation op4 = doc1.undo();
//        doc2.remoteUpdate(op4);
//        Operation op5 = doc2.localInsert(0, "anas", 1000);
//        doc1.remoteUpdate(op5);
//
//        Node[] nodes = doc1.sendTree();
//        CRDT_TREE doc3 = new CRDT_TREE("1","sawy",nodes);
//        Operation op6 = doc3.localInsert(0, "sawy", 10000);
//
//        doc2.remoteUpdate(op6);
//        doc1.remoteUpdate(op6);
//        doc1.printTree();
//        doc2.printTree();
//        doc3.printTree();

        System.out.println("doc1: "+doc1.getDocument());
//        System.out.println("doc2: "+doc2.getDocument());
//        System.out.println("doc3: "+doc3.getDocument());

    }




}
