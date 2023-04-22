package com.game.define;

/**
 *
 * @Description 服务器节点类型
 * @Date 2022/9/9 11:30
 **/
public enum App {

	GAME(1, "GAME_SERVER"),
	GATE(2, "GATE_SERVER"),
	CHAT(3, "CHAT_SERVER"),
	ROBOT(4, "ROBOT_SERVER"),
	ROBOT_CLIENT(5, "ROBOT_CLIENT"),
	;

	private int id;
	private String name;

	private App(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
