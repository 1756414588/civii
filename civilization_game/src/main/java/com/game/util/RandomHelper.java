package com.game.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.scheduling.annotation.Scheduled;


public class RandomHelper {
    static public boolean isHitRangeIn100(final int prob) {
        final int seed = randomInSize(100);
        boolean bool = false;
        if (seed < prob) {
            bool = true;
        }
        return bool;
    }

    static public int randomInSize(final int size) {
        return RandomUtils.nextInt(0, size);
    }

    static public long randomInSize(final long size) {
        return RandomUtils.nextLong(0, size);
    }

    static public boolean isSkillLevelUp(final int prob) {
        return randomInSize(1000) < prob;
    }

    //随机技能类型
    //1.攻击 2.防御 3.兵力 4.强攻 5.强防 6.攻城 7.守城
    // 最好读取配置 1~6 类型技能
    static public int randSkillType() {
        return RandomUtils.nextInt(1, 8);
    }

    //英雄洗练
    static public int randHeroRate() {
        return RandomUtils.nextInt(1, 1001);
    }


    //带rand参数随机
    public static int nextInt(Random rand, final int startInclusive, final int endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive,
                "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return startInclusive + rand.nextInt(endExclusive - startInclusive);
    }

    static public boolean isBattleActed(final int prob) {
        return randomInSize(1000) < prob;
    }

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    // 用在世界地图随机中
    public static int threadSafeRand(int min, int max) {
        max = Math.max(min, max);
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        return randomNum;
    }

    static public int randMonster(final int size) {
        return RandomUtils.nextInt(1, size + 1);
    }


    public static int randProperty(int min, int max) {
        max = Math.max(min, max);
        int randomNum = RandomUtils.nextInt(min, max + 1);
        return randomNum;
    }
}
