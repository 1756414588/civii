package com.game.domain.s;

public class StaticHeroProp {
    private int heroLv;
    private int heroType;
    private int attackFactor;
    private int defenceFactor;
    private int soldierFactor;


    public int getHeroLv() {
        return heroLv;
    }

    public void setHeroLv(int heroLv) {
        this.heroLv = heroLv;
    }




    public int getAttackFactor () {
        return attackFactor;
    }

    public void setAttackFactor (int attackFactor) {
        this.attackFactor = attackFactor;
    }

    public int getDefenceFactor () {
        return defenceFactor;
    }

    public void setDefenceFactor (int defenceFactor) {
        this.defenceFactor = defenceFactor;
    }

    public int getSoldierFactor () {
        return soldierFactor;
    }

    public void setSoldierFactor (int soldierFactor) {
        this.soldierFactor = soldierFactor;
    }

    public int getHeroType () {
        return heroType;
    }

    public void setHeroType (int heroType) {
        this.heroType = heroType;
    }
}
