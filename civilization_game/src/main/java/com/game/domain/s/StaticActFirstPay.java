package com.game.domain.s;

import java.util.List;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-7-24 上午10:52:49
 * @describe
 */
public class StaticActFirstPay {

	private int firstPay;
	private List<List<Integer>> awardList;
	private int moldId;

	public int getFirstPay() {
		return firstPay;
	}

	public void setFirstPay(int firstPay) {
		this.firstPay = firstPay;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

	public int getMoldId() {
		return moldId;
	}

	public void setMoldId(int moldId) {
		this.moldId = moldId;
	}
}
