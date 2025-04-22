
public class ID {

    private String agent;
    private long timestamp;

    public ID(String agent, long timestamp) {
        this.agent = agent;
        this.timestamp = timestamp;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "(" + agent + ", " + timestamp + ")";
    }
}
