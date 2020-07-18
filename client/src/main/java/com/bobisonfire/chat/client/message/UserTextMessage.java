package com.bobisonfire.chat.client.message;

public class UserTextMessage extends UserMessage {
    private final User sender;
    private final String message;

    public UserTextMessage(User sender, String message) {
        this.sender = sender;
        this.message = message;
        this.setType(Message.TEXT);
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getContent() {
        return sender.isPasta()
                ? ""
                : (sender.getColoredName() + ": ")
            + message;
    }
}
