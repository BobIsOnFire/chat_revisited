package com.bobisonfire.chat.client;

import java.io.IOException;
import java.net.Socket;

public interface ClientRunner {
    void open(Socket socket) throws IOException;
    void close();
}
