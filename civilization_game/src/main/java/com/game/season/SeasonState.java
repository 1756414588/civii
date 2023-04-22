package com.game.season;

public enum SeasonState {

	NO_OPEN(1, "未开启"),

	PREHEAT(2, "预热"),

	OPEN(3, "开启"),

	END(4, "结束"),

	EXHIB(5, "展示"),

	CLOSE(6, "关闭"),;

	private int state;
	private String desc;

	SeasonState(int state, String desc) {
		this.state = state;
		this.desc = desc;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public static SeasonState getSeasonState(int val) {
		for (SeasonState value : SeasonState.values()) {
			if (value.state == val) {
				return value;
			}
		}
		return null;
	}
}
