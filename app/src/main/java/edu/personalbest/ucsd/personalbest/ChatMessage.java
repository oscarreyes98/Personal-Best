package edu.personalbest.ucsd.personalbest;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String from;
    private String text;
    private Timestamp timestamp;

    public ChatMessage(String from, String text, Timestamp timestamp) {
        this.from = from;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


    public String toString() {
        return from +
                ":\n" +
                text +
                "\n" +
                "---\n";
    }
}
