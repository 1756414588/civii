package com.game.domain.s;

import java.util.List;

public class StaticAchievement {

    private int id;
    private int type;
    private int genre;
    private String name;
    private String desc;
    //private int cond;
    private int score;
    private List<Integer> award;
    private int nextId;
    private int first;
    private long target;

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

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    //public int getCond() {
    //    return cond;
    //}
    //
    //public void setCond(int cond) {
    //    this.cond = cond;
    //}

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }
}
