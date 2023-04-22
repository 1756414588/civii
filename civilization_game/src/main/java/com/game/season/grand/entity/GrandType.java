package com.game.season.grand.entity;

public enum GrandType {
	TYPE_1(1, "击杀100只虫族"),

	TYPE_2(2, "损兵或募兵"),

	TYPE_3(3, "参加战斗活动"),

	TYPE_4(4, "消耗钻石"),;

	int val;
	String desc;

	GrandType(int val, String desc) {
		this.val = val;
		this.desc = desc;
	}

	public int get() {
		return val;
	}
}
