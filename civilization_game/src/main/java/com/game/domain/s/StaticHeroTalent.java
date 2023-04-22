package com.game.domain.s;

import java.util.List;

public class StaticHeroTalent {

    private int id;
    private int talentType;
    private int level;
    private List<List<Integer>> consume;
    private List<Integer> effect;
    private int soldierType;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTalentType() {
        return talentType;
    }

    public void setTalentType(int talentType) {
        this.talentType = talentType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public List<Integer> getEffect() {
        return effect;
    }

    public void setEffect(List<Integer> effect) {
        this.effect = effect;
    }

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }
}
