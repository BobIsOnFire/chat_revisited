package com.bobisonfire.chat.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.bobisonfire.chat.client.EscapeExecutor.*;

public class Main {
    public static ResourceBundle R = ResourceBundle.getBundle("messages", Locale.getDefault());

    public static void main(String[] args) {
        if (args.length > 2) {
            LINES = Integer.parseInt(args[1]);
            COLUMNS = Integer.parseInt(args[2]);
        }

        String[] address = args[0].split(":");
        boolean addressCorrect = address.length == 2 &&
                    address[1].matches("^\\d+$") &&
                    Integer.parseInt(address[1]) > 10000 &&
                    Integer.parseInt(address[1]) < 65536;

        if (!addressCorrect)
            System.out.print(CLEAR_SCREEN + MOVE_LINE(1) + R.getString("error.address_incorrect") + "\r\n");
        else {
            try {
                Socket socket = new Socket(address[0], Integer.parseInt(address[1]));
                ClientRunner runner = new ChatClientRunner();
                try {
                    runner.open(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    runner.close();
                }
            } catch (IOException exc) {
                System.out.print(CLEAR_SCREEN + MOVE_LINE(1) + R.getString("error.cannot_connect") + "\r\n");
            }
        }
    }

    // todo make a basic client without ANSI and give a choice to switch between clients
}
