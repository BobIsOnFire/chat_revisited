package com.bobisonfire.chat.server.message;

import java.io.Serializable;

// todo move Message API in another module and make client and server depend on it
public class Message implements Serializable {
    public static final int SYSTEM = 1;
    public static final int TEXT = 2;
    public static final int IMAGE = 3;

    private int type = 0;

    Message() {}

    public int getType() {return type;}
    public void setType(int type) {
        this.type = type;
    }
    public String getContent() {return null;}

}
