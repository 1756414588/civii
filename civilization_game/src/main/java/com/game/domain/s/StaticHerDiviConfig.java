package com.game.domain.s;

import java.util.List;

public class StaticHerDiviConfig {

    private int id;
    private List<List<Integer>> equiplist;
    private List<List<Integer>> property;
    private int level;
    private int nextId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getEquiplist() {
        return equiplist;
    }

    public void setEquiplist(List<List<Integer>> equiplist) {
        this.equiplist = equiplist;
    }

    public List<List<Integer>> getProperty() {
        return property;
    }

    public void setProperty(List<List<Integer>> property) {
        this.property = property;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }
}
