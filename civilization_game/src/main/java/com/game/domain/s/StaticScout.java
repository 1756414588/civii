package com.game.domain.s;


public class StaticScout {
    private int type;
    private int scoutRate;
    private int showResRate;
    private int showSoldierRate;
    private int showHeroRate;

    private int addLevel;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScoutRate() {
        return scoutRate;
    }

    public void setScoutRate(int scoutRate) {
        this.scoutRate = scoutRate;
    }


    public int getAddLevel() {
        return addLevel;
    }

    public void setAddLevel(int addLevel) {
        this.addLevel = addLevel;
    }

    public int getShowResRate() {
        return showResRate;
    }

    public void setShowResRate(int showResRate) {
        this.showResRate = showResRate;
    }

    public int getShowSoldierRate() {
        return showSoldierRate;
    }

    public void setShowSoldierRate(int showSoldierRate) {
        this.showSoldierRate = showSoldierRate;
    }

    public int getShowHeroRate() {
        return showHeroRate;
    }

    public void setShowHeroRate(int showHeroRate) {
        this.showHeroRate = showHeroRate;
    }
}
