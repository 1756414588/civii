package com.game.domain.s;

import java.util.List;

// 兑换英雄
public class StaticExchangeHero {
    private int heroId;
    private List<List<Integer>> items;
    private List<List<Integer>> award;
    private List<Integer> loot1;
    private List<Integer> loot2;


    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public List<List<Integer>> getItems() {
        return items;
    }

    public void setItems(List<List<Integer>> items) {
        this.items = items;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<Integer> getLoot1() {
        return loot1;
    }

    public void setLoot1(List<Integer> loot1) {
        this.loot1 = loot1;
    }

    public List<Integer> getLoot2() {
        return loot2;
    }

    public void setLoot2(List<Integer> loot2) {
        this.loot2 = loot2;
    }
}
