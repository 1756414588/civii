package com.game.season.talent.entity;

import java.util.List;

public class StaticCompTalent {

	private int keyId;
	private List<List<Integer>> cost;
	private int total;
	private int prob;
	private int manReset;
	private int autoReset;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getProb() {
		return prob;
	}

	public void setProb(int prob) {
		this.prob = prob;
	}

	public int getManReset() {
		return manReset;
	}

	public void setManReset(int manReset) {
		this.manReset = manReset;
	}

	public int getAutoReset() {
		return autoReset;
	}

	public void setAutoReset(int autoReset) {
		this.autoReset = autoReset;
	}
}
