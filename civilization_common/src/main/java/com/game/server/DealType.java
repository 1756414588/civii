package com.game.server;

public enum DealType {
	PUBLIC(0, "PUBLIC") {
	},
	MAIN(1, "MAIN") {
	},
	SAVE_DATA(2, "SAVE_DATA") {
	},
	TIMER_LOGIC(3, "TIMER_LOGIC") {
	}
	;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private DealType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	private int code;
	private String name;
}
