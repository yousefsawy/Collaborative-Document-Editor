public class Item {


    public String content;
    public ID id;
    public ID originLeft;
    public ID originRight;
    public boolean deleted;


    public Item(String content, ID id, ID originLeft, ID originRight) {
        this.content = content;
        this.id = id;
        this.originLeft = originLeft;
        this.originRight = originRight;
        this.deleted = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public ID getOriginLeft() {
        return originLeft;
    }

    public void setOriginLeft(ID originLeft) {
        this.originLeft = originLeft;
    }

    public ID getOriginRight() {
        return originRight;
    }

    public void setOriginRight(ID originRight) {
        this.originRight = originRight;
    }
}
