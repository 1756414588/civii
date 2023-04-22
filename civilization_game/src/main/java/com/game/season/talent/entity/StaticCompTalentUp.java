package com.game.season.talent.entity;

import java.util.List;

public class StaticCompTalentUp {
	private int keyId;
	private int talentType;
	private int childType;
	private int typeId;
	private int lv;
	private List<List<Integer>> cost;
	private List<List<Integer>> effect;
	private int nextId;
	private List<Integer> preTypeId;
	private List<List<Integer>> totalCost;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getTalentType() {
		return talentType;
	}

	public void setTalentType(int talentType) {
		this.talentType = talentType;
	}

	public int getChildType() {
		return childType;
	}

	public void setChildType(int childType) {
		this.childType = childType;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public List<List<Integer>> getEffect() {
		return effect;
	}

	public void setEffect(List<List<Integer>> effect) {
		this.effect = effect;
	}

	public int getNextId() {
		return nextId;
	}

	public void setNextId(int nextId) {
		this.nextId = nextId;
	}

	public List<Integer> getPreTypeId() {
		return preTypeId;
	}

	public void setPreTypeId(List<Integer> preTypeId) {
		this.preTypeId = preTypeId;
	}

	public List<List<Integer>> getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(List<List<Integer>> totalCost) {
		this.totalCost = totalCost;
	}
}
