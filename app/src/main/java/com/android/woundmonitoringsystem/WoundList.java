package com.android.woundmonitoringsystem;

public class WoundList {
    private String type , size , color;

    public WoundList(String type, String size, String color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }
}
