package com.game.domain.s;

import java.util.List;

public class StaticKillEquipLevel {
    private int keyId;
    private int equipId;
    private int level;
    private int process;
    private List<List<Integer>> property;
    private int stone;
    private  List<List<Integer>> gold;


    public int getLevel () {
        return level;
    }

    public void setLevel (int level) {
        this.level = level;
    }

    public int getProcess () {
        return process;
    }

    public void setProcess (int process) {
        this.process = process;
    }

    public List<List<Integer>> getProperty () {
        return property;
    }

    public void setProperty (List<List<Integer>> property) {
        this.property = property;
    }

    public int getStone () {
        return stone;
    }

    public void setStone (int stone) {
        this.stone = stone;
    }

    public int getEquipId () {
        return equipId;
    }

    public void setEquipId (int equipId) {
        this.equipId = equipId;
    }

    public int getKeyId () {
        return keyId;
    }

    public void setKeyId (int keyId) {
        this.keyId = keyId;
    }

    public List<List<Integer>> getGold() {
        return gold;
    }

    public void setGold(List<List<Integer>> gold) {
        this.gold = gold;
    }
}
