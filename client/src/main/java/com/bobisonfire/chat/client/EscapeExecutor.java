package com.bobisonfire.chat.client;

class EscapeExecutor {
    static final String CLEAR_SCREEN = "\u001B[2J";
    static final String CLEAR_LINE = "\u001B[2K";
    static final String SCROLL_UP = "\u001B[S";

    static int LINES = -1;
    static int COLUMNS = -1;
    static final StringBuilder INPUT = new StringBuilder();

    static String DELETE_SYMBOLS(int amount) {
        return String.format("\u001B[%dD\u001B[K", amount + 1);
    }

    static String MOVE_LINE(int line) {
        return String.format("\u001B[%d;1H", line);
    }
}
