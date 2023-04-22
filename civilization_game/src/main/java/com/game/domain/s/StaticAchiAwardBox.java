package com.game.domain.s;

import java.util.List;

public class StaticAchiAwardBox {
    private int id;
    private int type;
    private int childType;
    private int cond;
    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChildType() {
        return childType;
    }

    public void setChildType(int childType) {
        this.childType = childType;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }
}
