package com.game.domain.s;

import java.util.List;

public class StaticActQuota {

	private int quotaId;
	private int awardId;
	private List<List<Integer>> awardList;
	private int display;
	private int price;
	private int count;
	private int cond;

	public int getQuotaId() {
		return quotaId;
	}

	public void setQuotaId(int quotaId) {
		this.quotaId = quotaId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

	public int getDisplay() {
		return display;
	}

	public void setDisplay(int display) {
		this.display = display;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

}
