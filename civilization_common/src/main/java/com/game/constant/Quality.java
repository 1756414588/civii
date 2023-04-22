package com.game.constant;


public enum Quality {
//    WHITE(1, "绿色"),
//    BLUE(2, "蓝色"),
//    GREEN(3, "紫色"),
//    GOLD(4, "橙色"),
//    RED(5, "红色"),
//    PURPLE(6, "彩色"),
    WHITE(1, "白色"),
    BLUE(2, "绿色"),
    GREEN(3, "蓝色"),
    GOLD(4, "紫色"),
    RED(5, "橙色"),
    PURPLE(6, "红色"),
    ;
    int val;
    String name;

    Quality(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public int get() {
        return val;
    }

    public static String getName(int quality) {
        for (Quality q : values()) {
            if (q.val == quality) {
                return q.name;
            }
        }
        return WHITE.name;
    }
}
