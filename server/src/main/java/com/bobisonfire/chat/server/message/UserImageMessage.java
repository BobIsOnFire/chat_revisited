package com.bobisonfire.chat.server.message;

import java.net.URL;

public class UserImageMessage extends UserMessage {
    private final User sender;
    private final URL address;

    public UserImageMessage(User sender, URL address) {
        this.sender = sender;
        this.address = address;
        this.setType(Message.TEXT);
    }

    public User getSender() {
        return sender;
    }

    public URL getAddress() {
        return address;
    }

    @Override
    public String getContent() {
        return sender.getColoredName() + ": image at " + address.toString() + "\n";
    }
}
