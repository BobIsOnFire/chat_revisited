package com.bobisonfire.chat.server.message;

public class LocalInfoMessage extends SystemMessage {
    String message;

    LocalInfoMessage() {}

    public LocalInfoMessage(String message) {
        super();
        this.message = message;
    }

    @Override
    public String getContent() {
        return message + "\r\n";
    }
}
