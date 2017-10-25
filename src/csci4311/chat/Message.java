package csci4311.chat;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private String from;
    private String message;
    private ArrayList<String> to;

    public Message(String from, ArrayList<String> to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getTo() {
        return to;
    }
}