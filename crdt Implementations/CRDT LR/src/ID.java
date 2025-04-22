public class ID {
    public String agent;
    public int seq;

    public ID(String agent, int seq) {
        this.agent = agent;
        this.seq = seq;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
