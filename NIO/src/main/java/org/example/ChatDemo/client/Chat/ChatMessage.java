package org.example.ChatDemo.client.Chat;

public class ChatMessage {
    private final String text;
    private final boolean self;

    public ChatMessage(String text, boolean self) {
        this.text = text;
        this.self = self;
    }

    public String getText() {
        return text;
    }

    public boolean isSelf() {
        return self;
    }
}

