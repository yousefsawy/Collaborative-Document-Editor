import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Doc {
    private ArrayList<Item> items;
    private Map<String, Integer> version;

    public Doc() {
        items = new ArrayList<>();
        version = new HashMap<>();
    }



    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            if (item != null && !item.deleted) {
                sb.append(item.getContent());
            }
        }
        return sb.toString();
    }

    public void localInsertOne(int position, String text, String agent) {
        int seq = 0;
        if (version.get(agent)!=null)
        {
            seq =version.get(agent)+1;
            version.put(agent,seq);
        }else {
            version.put(agent,0);
        }



        ID originLeft = null;
        ID originRight = null;

        if (position > 0 && position - 1 < items.size() && items.get(position - 1) != null) {
            originLeft = items.get(position - 1).getId();
        }

        if (position >= 0 && position < items.size() && items.get(position) != null) {
            originRight = items.get(position).getId();
        }

        Item newItem = new Item(text, new ID(agent, seq), originLeft, originRight);
        integrate(newItem);
    }

    public void remoteInsert(Item newItem){
        integrate(newItem);
    }

    public void localInsert(int position, String text, String agent){
        String[] chars = splitToCharArray(text);
        for (int i = 0; i < chars.length; i++) {
            localInsertOne(position,chars[i],agent);
            position++;
        }
    }

    public String[] splitToCharArray(String input) {
        String[] result = new String[input.length()];
        for (int i = 0; i < input.length(); i++) {
            result[i] = String.valueOf(input.charAt(i));
        }
        return result;
    }

    public void integrate(Item newItem) {
        int left = findItemIndexAtId(newItem.originLeft);
        int destIdx = left + 1;
        int right;
        if (newItem.originRight == null) {
            right = items.size();
        } else {
            right = findItemIndexAtId(newItem.originRight);
        }

        boolean scanning = false;
        for (int i = destIdx; i <= items.size(); i++) {
            if (!scanning) {
                destIdx = i;
            }

            if (i == items.size()) {
                break;
            }
            if (i == right) {
                break;
            }

            Item other = items.get(i);
            int oLeft = findItemIndexAtId(other.originLeft);
            int oRight;
            if (other.originRight == null) {
                oRight = items.size();
            } else {
                oRight = findItemIndexAtId(other.originRight);
            }

            if ((oLeft < left) || (oLeft == left && oRight == right && (newItem.getId().getAgent().compareTo(other.getId().getAgent()) < 0))) {
                break;
            }
            if (oLeft == left) {
                scanning = oRight < right;
            }
        }

        items.add(destIdx, newItem);
    }

    public int findItemIndexAtId(ID id) {
        if (id == null) return -1;
        for (int i = 0; i < items.size(); i++) {
            ID currentId = items.get(i).getId();
            if (idEq(currentId, id)) {
                return i;
            }
        }
        return -1;
    }

    public boolean idEq(ID a, ID b) {
        if (a == null || b == null) return false;
        return a.getAgent().equals(b.getAgent()) && a.getSeq() == b.getSeq();
    }

    public void printDoc() {
        System.out.println("Document Items:");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            System.out.printf(
                    "Index: %d | Content: '%s' | ID: (%s, %d) | originLeft: %s | originRight: %s | Deleted: %s\n",
                    i,
                    item.getContent(),
                    item.getId().getAgent(),
                    item.getId().getSeq(),
                    (item.getOriginLeft() != null) ? String.format("(%s, %d)", item.getOriginLeft().getAgent(), item.getOriginLeft().getSeq()) : "null",
                    (item.getOriginRight() != null) ? String.format("(%s, %d)", item.getOriginRight().getAgent(), item.getOriginRight().getSeq()) : "null",
                    item.deleted ? "true" : "false"
            );
        }
    }

}
