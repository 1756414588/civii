package com.game.domain.s;

import java.util.List;

public class StaticFortressBuild {
    private int id;
    private int times;
    private List<List<Integer>> needProp;
    private int exp;
    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public List<List<Integer>> getNeedProp() {
        return needProp;
    }

    public void setNeedProp(List<List<Integer>> needProp) {
        this.needProp = needProp;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }
}
