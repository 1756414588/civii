package com.game.log.constant;

public enum RoleExpType{
	MISSION(1,"副本"),

	STAR3_MISSION(2,"副本三星"),

	TASK(3,"任务"),

	REBUILD(4,"建筑"),

	ACT(5,"活动"),

	MAIL(6,"邮件"),

	GM(7,"GM命令"),

	CREATE(8,"账号创建")
	;
	
	private Integer code;

	private String desc;

	RoleExpType(int code, String desc) {
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
