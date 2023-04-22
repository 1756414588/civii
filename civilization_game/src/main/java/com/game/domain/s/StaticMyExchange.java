package com.game.domain.s;

import java.util.List;

public class StaticMyExchange {

    private int id;
    private int awardId;
    private List<Integer> cost;
    private List<List<Integer>> award;
    private int maxChangeTimes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public List<Integer> getCost() {
        return cost;
    }

    public void setCost(List<Integer> cost) {
        this.cost = cost;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getMaxChangeTimes() {
        return maxChangeTimes;
    }

    public void setMaxChangeTimes(int maxChangeTimes) {
        this.maxChangeTimes = maxChangeTimes;
    }
}
