package com.game.domain.s;

import java.util.List;

public class StaticBulletWarLevel {

	private int id;
	private List<List<Integer>> award;

	private List<List<Integer>> extra_award;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<List<Integer>> getAward() {
		return award;
	}

	public void setAward(List<List<Integer>> award) {
		this.award = award;
	}

	public List<List<Integer>> getExtra_award() {
		return extra_award;
	}

	public void setExtra_award(List<List<Integer>> extra_award) {
		this.extra_award = extra_award;
	}
}
