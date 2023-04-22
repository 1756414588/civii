package com.game.constant;


public enum SoldierName {
    ROCKET(1, "步兵"),
    TANK(2, "坦克兵"),
    WARCAR(3, "炮兵"),
    ;
    int val;
    String name;

    SoldierName(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public static String getName(int val) {
        for (SoldierName name : values()) {
            if (name.val == val) {
                return name.name;
            }
        }
        return SoldierName.ROCKET.name;
    }
}
