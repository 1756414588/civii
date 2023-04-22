package com.game.log.constant;

public enum HeroExpType{
	MISSION(1,"副本"),

	MONSTER(2,"世界野怪"),

	COUNTRY_WAR(3,"阵营战"),

	CITY_WAR(4,"城战"),

	PROP(5,"道具");
	
	private Integer code;

	private String desc;

	HeroExpType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
