package com.game.domain.s;

import java.util.List;

public class StaticLairRank {

    private int id;
    private int type;
    private List<Integer> rankRand;
    private List<List<Integer>> award;
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getRankRand() {
        return rankRand;
    }

    public void setRankRand(List<Integer> rankRand) {
        this.rankRand = rankRand;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
