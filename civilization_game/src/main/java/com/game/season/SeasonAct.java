package com.game.season;

public enum SeasonAct {

	ACT_1(601, "宏伟宝库"),

	ACT_2(602, "赛季旅程"),

	ACT_3(603, "赛季转盘"),

	ACT_4(604, "七天任务"),

	ACT_5(605, "直升礼包"),

	ACT_7(607, "赛季礼包"),

	ACT_8(608, "赛季月卡"),

	ACT_10(610, "赛季天赋"),;

	private int actId;
	private String desc;

	SeasonAct(int actId, String desc) {
		this.actId = actId;
		this.desc = desc;
	}

	public int getActId() {
		return actId;
	}

	public void setActId(int actId) {
		this.actId = actId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
