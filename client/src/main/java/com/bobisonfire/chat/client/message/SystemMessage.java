package com.bobisonfire.chat.client.message;

import com.bobisonfire.chat.client.Main;

public class SystemMessage extends Message {
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    private final int severity;
    private final String messageCode;

    SystemMessage() {
        this(INFO, "");
    }

    public SystemMessage(int severity, String messageCode) {
        this.severity = severity;
        this.messageCode = messageCode;
        setType(Message.SYSTEM);
    }

    public int getSeverity() {
        return severity;
    }

    public String getMessageCode() {
        return messageCode;
    }


    @Override
    public String getContent() {
        int color = switch (severity) {
            default -> 7;
            case WARN -> 3;
            case ERROR -> 1;
        };

        return "\u001B[3" + color + "m" + Main.R.getString(messageCode) + "\u001B[37m\r\n";
    }
}
