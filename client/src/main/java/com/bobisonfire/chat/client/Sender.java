package com.bobisonfire.chat.client;

import com.bobisonfire.chat.client.message.Message;

import java.io.IOException;

public interface Sender extends Runnable {
    Message read() throws IOException;
    void send(Message msg);
    void stop();
    void setReceiver(Receiver receiver); // todo set up a bridge to avoid high coupling
}
