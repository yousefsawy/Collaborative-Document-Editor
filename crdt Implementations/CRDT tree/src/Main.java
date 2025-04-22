public class Main {
    public static void main(String[] args) {
        Doc doc = new Doc();
        doc.localInsertOne(0,"a","omar");
        doc.localInsertOne(1,"b","omar");
        doc.localInsertOne(0,"c","omar");
        doc.localInsertOne(2,"d","test");
        System.out.println("doc has content : "+ doc.getContent());
        doc.printDoc();
    }
}