package com.game.recharge.utils;

import java.util.Random;
import java.util.UUID;

public class RandomHelper {
    static public boolean isHitRangeIn100(final int prob) {
        final int seed = new Random().nextInt(100);
        boolean bool = false;
        if (seed < prob) {
            bool = true;
        }
        return bool;
    }

    static public boolean isHitRangeIn1000(final int prob) {
        final int seed = new Random().nextInt(1000);
        boolean bool = false;
        if (seed < prob) {
            bool = true;
        }
        return bool;
    }

    static public int randomInSize(final int size) {
        return new Random().nextInt(size);
    }

    static public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
