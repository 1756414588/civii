package com.game.flame.entity;

import java.util.List;

public class StaticFlameShop {
	private int id;
	private int sort;
	private int needScore;
	private List<Integer> prop;
	private int limit;
	private int chat;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public int getNeedScore() {
		return needScore;
	}

	public void setNeedScore(int needScore) {
		this.needScore = needScore;
	}

	public List<Integer> getProp() {
		return prop;
	}

	public void setProp(List<Integer> prop) {
		this.prop = prop;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getChat() {
		return chat;
	}

	public void setChat(int chat) {
		this.chat = chat;
	}
}
