package com.bobisonfire.chat.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ServerRunner {
    void open() throws IOException;
    void handleAccept(SelectionKey key) throws IOException;
    void handleRead(SelectionKey key) throws IOException;
    void close();
}
