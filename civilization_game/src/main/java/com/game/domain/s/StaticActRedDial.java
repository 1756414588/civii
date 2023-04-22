package com.game.domain.s;

import java.util.List;

// 红装转盘
public class StaticActRedDial {
    private int keyId;
    private int awardId;
    private List<Integer> price;
    private List<List<Integer>> lootRate1;
    private List<List<Integer>> lootRate2;
    private List<Integer> mustLoot;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public List<Integer> getPrice() {
        return price;
    }

    public void setPrice(List<Integer> price) {
        this.price = price;
    }

    public List<List<Integer>> getLootRate1() {
        return lootRate1;
    }

    public void setLootRate1(List<List<Integer>> lootRate1) {
        this.lootRate1 = lootRate1;
    }

    public List<List<Integer>> getLootRate2() {
        return lootRate2;
    }

    public void setLootRate2(List<List<Integer>> lootRate2) {
        this.lootRate2 = lootRate2;
    }

    public List<Integer> getMustLoot() {
        return mustLoot;
    }

    public void setMustLoot(List<Integer> mustLoot) {
        this.mustLoot = mustLoot;
    }
}
