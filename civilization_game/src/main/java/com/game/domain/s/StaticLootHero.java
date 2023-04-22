package com.game.domain.s;


public class StaticLootHero {
    private int lootId;
    private int awardId;
    private int lootType;
    private int price;
    private int additionAwardId;
    private int threeLoot;
    private int FourLoot1;
    private int FourLoot2;

    public int getLootId () {
        return lootId;
    }

    public void setLootId (int lootId) {
        this.lootId = lootId;
    }

    public int getAwardId () {
        return awardId;
    }

    public void setAwardId (int awardId) {
        this.awardId = awardId;
    }

    public int getLootType () {
        return lootType;
    }

    public void setLootType (int lootType) {
        this.lootType = lootType;
    }

    public int getPrice () {
        return price;
    }

    public void setPrice (int price) {
        this.price = price;
    }

    public int getAdditionAwardId () {
        return additionAwardId;
    }

    public void setAdditionAwardId (int additionAwardId) {
        this.additionAwardId = additionAwardId;
    }

    public int getThreeLoot() {
        return threeLoot;
    }

    public void setThreeLoot(int threeLoot) {
        this.threeLoot = threeLoot;
    }

    public int getFourLoot1() {
        return FourLoot1;
    }

    public void setFourLoot1(int fourLoot1) {
        FourLoot1 = fourLoot1;
    }

    public int getFourLoot2() {
        return FourLoot2;
    }

    public void setFourLoot2(int fourLoot2) {
        FourLoot2 = fourLoot2;
    }
}
