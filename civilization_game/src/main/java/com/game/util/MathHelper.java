package com.game.util;

public class MathHelper {
    public static int devide(int a, int b) {
        return (a / b) + ((a % b == 0) ? 0 : 1);
    }

    public static boolean isEqual(double d1, double d2) {
        long thisBits    = Double.doubleToLongBits(d1);
        long anotherBits = Double.doubleToLongBits(d2);

        return thisBits == anotherBits;
    }

}
