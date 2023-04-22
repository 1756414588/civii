package com.game.season.hero;

import java.util.List;

public class StaticComProf {

	private int profId;
	private int state;
	private int star;
	private String desc;
	private List<List<Integer>> upgrade;
	private int nextId;
	private List<List<Integer>> addBasicPro;

    public int getProfId() {
        return profId;
    }

    public void setProfId(int profId) {
        this.profId = profId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<List<Integer>> getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(List<List<Integer>> upgrade) {
        this.upgrade = upgrade;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public List<List<Integer>> getAddBasicPro() {
        return addBasicPro;
    }

    public void setAddBasicPro(List<List<Integer>> addBasicPro) {
        this.addBasicPro = addBasicPro;
    }
}
