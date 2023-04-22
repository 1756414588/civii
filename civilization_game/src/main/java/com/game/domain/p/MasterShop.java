package com.game.domain.p;

import java.util.HashMap;
import java.util.Map;

public class MasterShop {
    Map<Integer, Integer> MasterShopAward = new HashMap<>();
    long score;

    public Map<Integer, Integer> getMasterShopAward() {
        return MasterShopAward;
    }

    public void setMasterShopAward(Map<Integer, Integer> masterShopAward) {
        MasterShopAward = masterShopAward;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}
