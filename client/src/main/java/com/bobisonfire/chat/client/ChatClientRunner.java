package com.bobisonfire.chat.client;

import java.io.IOException;
import java.net.Socket;

public class ChatClientRunner implements ClientRunner {
    private Receiver receiver;
    private Sender sender;
    private Socket socket;

    @Override
    public void open(Socket socket) throws IOException {
        this.socket = socket;

        receiver = new ChatReceiver(socket);
        sender = new ChatSender(socket);

        receiver.setSender(sender);
        sender.setReceiver(receiver);

        new Thread(receiver).start();
        new Thread(sender).start();
    }

    @Override
    public void close() {
        if (receiver != null) receiver.stop();
        if (sender != null) sender.stop();
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
