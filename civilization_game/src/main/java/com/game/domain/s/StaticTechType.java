package com.game.domain.s;

public class StaticTechType {
    private int techType;
    private int minLevel;
    private int maxLevel;
    public int getTechType() {
        return techType;
    }

    public void setTechType(int techType) {
        this.techType = techType;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMinLevel () {
        return minLevel;
    }

    public void setMinLevel (int minLevel) {
        this.minLevel = minLevel;
    }
}
