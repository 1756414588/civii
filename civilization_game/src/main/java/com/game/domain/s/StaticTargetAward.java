package com.game.domain.s;

import java.util.List;

public class StaticTargetAward {
	private int targetId;
	private List<List<Integer>> awards;

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public List<List<Integer>> getAwards() {
		return awards;
	}

	public void setAwards(List<List<Integer>> awards) {
		this.awards = awards;
	}

}
