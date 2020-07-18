package com.bobisonfire.chat.client;

import com.bobisonfire.chat.client.message.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Locale;

import static com.bobisonfire.chat.client.EscapeExecutor.*;

public class ChatSender implements Sender {
    private final PrintWriter out;
    private final User user;
    private final Gson gson;
    private Receiver receiver;

    private int lastSymbol;
    private boolean cyrillic;
    private byte cyrillicBegin;
    private boolean stopped;

    public ChatSender(Socket socket) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new Gson();

        lastSymbol = 10;
        cyrillic = false;
        stopped = false;

        String name = readInput().trim();
        this.user = new User(name);
        send(new UserTextMessage(this.user, ""));

        System.out.print(CLEAR_SCREEN + MOVE_LINE(1));
    }

    @Override
    public Message read() throws IOException {
        return parseInput(readInput());
    }

    @Override
    public void send(Message msg) {
        if (msg == null) return;
        out.println(gson.toJson(msg, msg.getClass()));
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {
        while(!this.stopped) {
            try {
                send(read());
            } catch (IOException e) {
                stop();
                receiver.stop();
            }
        }
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    private String readInput() throws IOException {
        while(true) {
            lastSymbol = System.in.read();

            if (!cyrillic && lastSymbol > 127) {
                cyrillic = true;
                cyrillicBegin = (byte) lastSymbol;
                continue;
            }

            if (lastSymbol == 127 || lastSymbol == 8) {
                System.out.print(DELETE_SYMBOLS(1));
                if (INPUT.length() > 0) {
                    INPUT.delete(INPUT.length() - 1, INPUT.length());
                }
                continue;
            }

            if (cyrillic) {
                cyrillic = false;
                INPUT.append( new String(new byte[]{cyrillicBegin, (byte) lastSymbol}) );
                continue;
            }

            if (user != null && user.isPasta() && lastSymbol == 13) {
                INPUT.append("\r\n");
                continue;
            }

            boolean endOfMessage = lastSymbol == 12 || lastSymbol == 3 || lastSymbol == 4 ||
                    (user == null || !user.isPasta()) && (lastSymbol == 10 || lastSymbol == 13);

            if (endOfMessage) {
                String str = INPUT.toString();
                INPUT.delete(0, INPUT.length());
                return str;
            }

            INPUT.append( (char) lastSymbol );
        }
    }

    private Message parseInput(String input) {
        String line = input.trim();
        if (line.startsWith("/exit") || lastSymbol == 3 || lastSymbol == 4) {
            stop();
            receiver.stop();
            return new UserTextMessage(user, "");
        }

        if (!line.startsWith("/")) {
            return new UserTextMessage(user, input + "\n");
        }

        Message message = null;

        if (line.startsWith("/pasta")) {
            user.setPasta(true);
            message = new SystemMessage(SystemMessage.INFO, "client.pasta_mode.enabled");
        }

        if (line.startsWith("/unpasta")) {
            user.setPasta(false);
            message = new SystemMessage(SystemMessage.INFO, "client.pasta_mode.disabled");
        }

        if (line.startsWith("/color ")) {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2 || !tokens[1].matches("\\d+") ||
                    Integer.parseInt(tokens[1]) < 0 || Integer.parseInt(tokens[1]) > 255) {
                message = new SystemMessage(SystemMessage.ERROR, "error.wrong_color_attribute");
            } else {
                user.setColor(Integer.parseInt(tokens[1]));
                message = new LocalInfoMessage("\u001B[38;5;" + user.getColor() + "m" +
                        Main.R.getString("client.color_changed") + "\u001B[39m");
            }
        }

        if (line.startsWith("/image ")) {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                message = new SystemMessage(SystemMessage.ERROR, "error.no_path_attribute");
            } else {
                try {
                    return new UserImageMessage(user, new URL(tokens[1]));
                } catch (MalformedURLException e) {
                    message = new SystemMessage(SystemMessage.ERROR, "error.invalid_url");
                }
            }
        }

        if (line.startsWith("/console ")) {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 3) {
                message = new SystemMessage(SystemMessage.ERROR, "error.not_enough_arguments");
            } else {
                LINES = Integer.parseInt(tokens[1]);
                COLUMNS = Integer.parseInt(tokens[2]);
                message = new SystemMessage(SystemMessage.INFO, "client.console_size_changed");
            }
        }

        if (line.startsWith("/help")) {
            String help = String.format(Locale.getDefault(),
                    "/exit - %s\n" +
                    "/pasta - %s\n" +
                    "/unpasta - %s\n" +
                    "/color CODE - %s\n" +
                    "/console LINES COLUMNS - %s\n" +
                    "/image URL - %s\n",
                    Main.R.getString("client.help.exit"),
                    Main.R.getString("client.help.pasta"),
                    Main.R.getString("client.help.unpasta"),
                    Main.R.getString("client.help.color"),
                    Main.R.getString("client.help.console"),
                    Main.R.getString("client.help.image")
            );

            message = new LocalInfoMessage(help);
        }

        if (message == null) message = new SystemMessage(SystemMessage.ERROR, "error.invalid_command");
        receiver.print(message);
        return null;
    }
}
