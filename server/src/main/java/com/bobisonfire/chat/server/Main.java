package com.bobisonfire.chat.server;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main {
    public static ResourceBundle R = ResourceBundle.getBundle("messages", Locale.getDefault());

    public static void main(String[] args) {
        ServerRunner runner = new ChatServerRunner();
        try {
            runner.open();
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            runner.close();
        }
    }
}
