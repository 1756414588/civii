package com.game.domain.s;

import java.util.List;

// 挖宝
public class StaticPvpDig {
    private int keyId;
    private int buyTimes;
    private int digCost;
    private int buyCost;
    private int lootPaperRate;
    private List<List<Integer>> lootProp;
    private List<Integer> lootPaper;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getBuyTimes() {
        return buyTimes;
    }

    public void setBuyTimes(int buyTimes) {
        this.buyTimes = buyTimes;
    }

    public int getDigCost() {
        return digCost;
    }

    public void setDigCost(int digCost) {
        this.digCost = digCost;
    }

    public int getBuyCost() {
        return buyCost;
    }

    public void setBuyCost(int buyCost) {
        this.buyCost = buyCost;
    }

    public int getLootPaperRate() {
        return lootPaperRate;
    }

    public void setLootPaperRate(int lootPaperRate) {
        this.lootPaperRate = lootPaperRate;
    }

    public List<List<Integer>> getLootProp() {
        return lootProp;
    }

    public void setLootProp(List<List<Integer>> lootProp) {
        this.lootProp = lootProp;
    }

    public List<Integer> getLootPaper() {
        return lootPaper;
    }

    public void setLootPaper(List<Integer> lootPaper) {
        this.lootPaper = lootPaper;
    }
}
