package com.game.domain.s;

import java.util.List;

public class StaticFriendshipScoreShop {
    private int id;
    private List<Integer> award;
    private int openLevel;
    private int needLevel;
    private long needScore;
    private int maxExchangeNum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getOpenLevel() {
        return openLevel;
    }

    public void setOpenLevel(int openLevel) {
        this.openLevel = openLevel;
    }

    public int getNeedLevel() {
        return needLevel;
    }

    public void setNeedLevel(int needLevel) {
        this.needLevel = needLevel;
    }

    public long getNeedScore() {
        return needScore;
    }

    public void setNeedScore(long needScore) {
        this.needScore = needScore;
    }

    public int getMaxExchangeNum() {
        return maxExchangeNum;
    }

    public void setMaxExchangeNum(int maxExchangeNum) {
        this.maxExchangeNum = maxExchangeNum;
    }
}
