package com.game.domain.s;

import java.util.List;

// 杀器暴击概率
public class StaticKillEquipRate {
    private int criti;
    private List<Integer> rate;

    public int getCriti () {
        return criti;
    }

    public void setCriti (int criti) {
        this.criti = criti;
    }

    public List<Integer> getRate () {
        return rate;
    }

    public void setRate (List<Integer> rate) {
        this.rate = rate;
    }
}
