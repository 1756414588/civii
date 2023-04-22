package com.game.season.seven.entity;

public enum SevenType {
	MISSION(1, "通关战役"),

	MILITARY(2, "通关远征"),

	USE_GOLD(3, "消耗钻石"),

	WORM(4, "剿灭虫族"),

	BIG_WORM(5, "剿灭巨型虫族"),

	HERO(6, "获得赛季英雄"),

	HERO_MILI(7, "赛季英雄军职升级"),

	HERO_TEC_LEVEL(8, "赛季英雄技能升级"),

	HERO_LEVEL(9, "赛季英雄等级升级"),

	FREE_TRAIN(10, "免费特训"),

	GOLD_TRAIN(11, "钻石特训"),

	RESOURCE(12, "采集资源"),

	MAKE_EUQ(13, "装备打造"),

	REFORM_EUQ(14, "装备改造"),

	WAR(15, "参加阵营战"),

	KILL(16, "杀敌数量"),

	LOSS(17, "损兵数量"),

	;

	int val;
	String desc;

	SevenType(int val, String desc) {
		this.val = val;
		this.desc = desc;
	}

	public int get() {
		return val;
	}
}
