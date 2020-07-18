package com.bobisonfire.chat.client.message;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private boolean pasta;
    private int color;

    User() {
        this.name = "";
        this.pasta = true;
        this.color = 7;
    }

    public User(String name) {
        this.name = name;
        this.pasta = false;
        this.color = (int) (Math.random() * 255);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPasta() {
        return pasta;
    }

    public void setPasta(boolean pasta) {
        this.pasta = pasta;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getColoredName() {
        return "\u001B[38;5;" + color + "m" + name + "\u001B[39m";
    }
}
