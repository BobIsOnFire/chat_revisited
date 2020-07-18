package com.bobisonfire.chat.client;

import com.bobisonfire.chat.client.message.Message;

import java.io.IOException;

public interface Receiver extends Runnable {
    Message receive() throws IOException;
    void print(Message msg);
    void stop();
    void setSender(Sender sender);
}
