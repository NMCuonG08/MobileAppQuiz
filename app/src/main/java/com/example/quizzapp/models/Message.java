package com.example.quizzapp.models;

import android.text.Spanned;

public class Message {
    private Spanned content;
    private String sender; // "user" or "bot"

    public Message(Spanned content, String sender) {
        this.content = content;
        this.sender = sender;
    }

    public Spanned getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }
}
