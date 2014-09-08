package ${groupId}.plugin;

import java.io.Serializable;

/**
 * @author smecsia
 */
public class State implements Serializable {
    private String lastMessage;

    public State() {
    }

    public State(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
