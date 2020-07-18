package com.bobisonfire.chat.server;

import com.bobisonfire.chat.server.message.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ChatServerRunner implements ServerRunner {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);
    private final Gson gson = new Gson();

    @Override
    public void open() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(0));
        serverSocketChannel.configureBlocking(false);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Opening server on " + InetAddress.getLocalHost() +
                ":" + serverSocketChannel.socket().getLocalPort());

        while (serverSocketChannel.isOpen()) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) handleAccept(key);
                if (key.isReadable()) handleRead(key);
            }
        }
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = socketChannel.socket().getInetAddress().toString() + ":" + socketChannel.socket().getPort();

        System.out.println("Received connection from " + address);
        Message msg = new SystemMessage(SystemMessage.INFO, "client.join");
        socketChannel.write( ByteBuffer.wrap((gson.toJson(msg, msg.getClass()) + "\n").getBytes()) );

        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        UserTextMessage response = (UserTextMessage) applyType(new String(bytes));

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, response.getSender());

        msg = new SystemMessage(SystemMessage.INFO, "client.chat_enter");
        socketChannel.write( ByteBuffer.wrap((gson.toJson(msg, msg.getClass()) + "\n").getBytes()) );
        broadcast(new UserTextMessage(response.getSender(), "entered the chat.\n"));
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        StringBuilder sb = new StringBuilder();

        buffer.clear();
        try {
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                sb.append(new String(bytes));
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            UserMessage msg = (UserMessage) applyType(sb.toString());
            key.attach(msg.getSender());
            broadcast(msg);
        } catch (NullPointerException exc) {
            socketChannel.close();
            broadcast(new UserTextMessage((User) key.attachment(), "left the chat.\n"));
        }
    }

    @Override
    public void close() {
        try {
            serverSocketChannel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(Message msg) throws IOException {
        System.out.print(msg.getContent());
        ByteBuffer broadcastBuffer = ByteBuffer.wrap( (gson.toJson(msg, msg.getClass()) + "\n").getBytes() );

        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                socketChannel.write(broadcastBuffer);
                broadcastBuffer.rewind();
            }
        }
    }

    private Message applyType(String json) {
        Message msg = gson.fromJson(json, Message.class);

        Class<? extends Message> clazz = switch (msg.getType()) {
            case Message.SYSTEM -> SystemMessage.class;
            case Message.TEXT -> UserTextMessage.class;
            case Message.IMAGE -> UserImageMessage.class;
            default -> throw new IllegalStateException("Unexpected value: " + msg.getType());
        };

        return gson.fromJson(json, clazz);
    }
}
