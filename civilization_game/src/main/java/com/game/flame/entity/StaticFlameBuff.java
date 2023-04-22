package com.game.flame.entity;

import java.util.List;

public class StaticFlameBuff {

	private int id;
	private int type;
	private String name;
	private int lv;
	private int nextBuffId;
	private List<List<Integer>> effect;
	private int cost;
	// private String desc;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}


	public int getNextBuffId() {
		return nextBuffId;
	}

	public void setNextBuffId(int nextBuffId) {
		this.nextBuffId = nextBuffId;
	}

	public List<List<Integer>> getEffect() {
		return effect;
	}

	public void setEffect(List<List<Integer>> effect) {
		this.effect = effect;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}
}
