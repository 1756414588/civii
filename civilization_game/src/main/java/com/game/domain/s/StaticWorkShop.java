package com.game.domain.s;

import java.util.List;

public class StaticWorkShop {
    private int level;
    private int quality;
    private List<List<Long>> resource;
    private List<List<Integer>> lootList;
    private List<Integer> time;
    private List<List<Integer>> lootRate;
    private int commandLv;
    private int lordLv;

    public int getLevel () {
        return level;
    }

    public void setLevel (int level) {
        this.level = level;
    }

    public int getQuality () {
        return quality;
    }

    public void setQuality (int quality) {
        this.quality = quality;
    }

    public List<List<Integer>> getLootList () {
        return lootList;
    }

    public void setLootList (List<List<Integer>> lootList) {
        this.lootList = lootList;
    }

    public List<List<Integer>> getLootRate () {
        return lootRate;
    }

    public void setLootRate (List<List<Integer>> lootRate) {
        this.lootRate = lootRate;
    }

    public int getCommandLv () {
        return commandLv;
    }

    public void setCommandLv (int commandLv) {
        this.commandLv = commandLv;
    }

    public int getLordLv () {
        return lordLv;
    }

    public void setLordLv (int lordLv) {
        this.lordLv = lordLv;
    }

    public List<Integer> getTime () {
        return time;
    }

    public void setTime (List<Integer> time) {
        this.time = time;
    }

    public List<List<Long>> getResource () {
        return resource;
    }

    public void setResource (List<List<Long>> resource) {
        this.resource = resource;
    }
}
