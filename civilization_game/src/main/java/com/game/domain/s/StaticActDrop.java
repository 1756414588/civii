package com.game.domain.s;

import java.util.List;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-3-9 下午5:01:17
 * @describe
 */
public class StaticActDrop {

	private int keyId;
	private int awardId;
	private int probability;
	private List<List<Integer>> dropList;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public List<List<Integer>> getDropList() {
		return dropList;
	}

	public void setDropList(List<List<Integer>> dropList) {
		this.dropList = dropList;
	}

}
