package com.bobisonfire.chat.client;

import com.bobisonfire.chat.client.message.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

import static com.bobisonfire.chat.client.EscapeExecutor.*;

public class ChatReceiver implements Receiver {
    private final BufferedReader in;
    private final Gson gson;
    private Sender sender;

    private boolean stopped;
    private int line;
    private String cache;

    public ChatReceiver(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.gson = new Gson();

        this.stopped = false;
        this.line = 2;
        this.cache = "";

        Message msg = receive();
        if (msg == null) throw new IOException();
        System.out.print(msg.getContent());
    }

    @Override
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Override
    public Message receive() throws IOException {
        String line = in.readLine();
        return applyType(line);
    }

    @Override
    public void print(Message msg) {
        if (msg == null) return;

        String[] messageLines = (msg.getContent() + " ").split("\n");
        messageLines[0] = cache + messageLines[0];
        cache = messageLines[messageLines.length - 1];
        cache = cache.substring(0, cache.length() - 1);
        messageLines = Arrays.copyOf(messageLines, messageLines.length - 1);

        for (String s : messageLines) {
            if (s.startsWith("\u001B")) {
                writeLine(s);
                continue;
            }

            int k = s.length() / COLUMNS + 1;
            for (int i = 0; i < k; ++i) {
                String lineContent = (i < k - 1) ? s.substring(i * COLUMNS, (i + 1) * COLUMNS) : s.substring(i * COLUMNS);
                writeLine(lineContent);
            }
        }

        System.out.print(MOVE_LINE(1) + CLEAR_LINE + INPUT.toString());
    }

    @Override
    public void stop() {
        stopped = true;
        System.out.print(CLEAR_SCREEN + MOVE_LINE(1));
    }

    @Override
    public void run() {
        while(!this.stopped) {
            try {
                print(receive());
            } catch (IOException e) {
                stop();
                sender.stop();
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

    private void writeLine(String lineContent) {
        if (line > LINES) {
            System.out.print(MOVE_LINE(line) + SCROLL_UP + CLEAR_LINE + lineContent);
        } else {
            System.out.print(MOVE_LINE(line) + CLEAR_LINE + lineContent);
            ++line;
        }
    }
}
