package com.game.domain.p;

import java.util.Comparator;

/**
 *
 * @date 2020/10/28 18:18
 * @description
 */
public class CtyRankDesc implements Comparator<CtyRank> {
    @Override
    public int compare(CtyRank o1, CtyRank o2) {
        if (o1.getV() < o2.getV()) {
            return 1;
        }

        if (o1.getV() > o2.getV()) {
            return -1;
        }

        if (o1.getTime() < o2.getTime()) {
            return -1;
        }

        if (o1.getTime() > o2.getTime()) {
            return 1;
        }

        if (o1.getLordId() > o2.getLordId()) {
            return 1;
        }

        if (o1.getLordId() < o2.getLordId()) {
            return -1;
        }

        return 0;
    }
}
