package com.game.flame;

public enum NodeType {

	Player(3, "玩家"), City(102, "城池"), Mine(103, "采集点");

	private int type;
	private String desc;

	NodeType(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static NodeType getNodeType(int type) {
		for (NodeType wet : NodeType.values()) {
			if (wet.getType() == type) {
				return wet;
			}
		}
		return null;
	}

	public int getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}
}
