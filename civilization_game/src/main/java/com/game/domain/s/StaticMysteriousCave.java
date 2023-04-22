package com.game.domain.s;

import java.util.List;

// 神秘矿洞
public class StaticMysteriousCave {
    private int id;
    private int cd;
    private int rate;
    private List<List<Integer>> award;

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public int getCd () {
        return cd;
    }

    public void setCd (int cd) {
        this.cd = cd;
    }

    public int getRate () {
        return rate;
    }

    public void setRate (int rate) {
        this.rate = rate;
    }

    public List<List<Integer>> getAward () {
        return award;
    }

    public void setAward (List<List<Integer>> award) {
        this.award = award;
    }
}
