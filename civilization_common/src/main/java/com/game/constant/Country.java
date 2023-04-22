package com.game.constant;

public enum Country {
    //MONSTER(0, "虫族"),
    FEDERAL(1, "联邦"),
    EMPIRE(2, "帝国"),
    REPUBLIC(3, "共和"),
    ;

    Country(int key, String val) {
        this.key = key;
        this.val = val;
    }

    int key;
    String val;

    public static String get(int key) {
        for (Country value : Country.values()) {
            if (value.key == key) {
                return value.val;
            }
        }
        return null;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
