package com.game.enumerate;

public enum SearchTokeType {
	SearchTokeOne(1, 1), SearchTokeTen(2, 10), AdvancedsearchTokeone(3, 1), AdvancedsearchTokeTen(4, 10);
	// 成员变量
	private int type;
	private int index;

	private SearchTokeType(int type, int index) {
		this.type = type;
		this.index = index;
	}

	
	
	public static SearchTokeType getType(int index) {
		for (SearchTokeType searchToke : SearchTokeType.values()) {
			if (searchToke.getIndex() == index) {
				return searchToke;
			}
		}
		return null;
	}
	
	public static SearchTokeType getIndex(int type) {
		for (SearchTokeType searchToke : SearchTokeType.values()) {
			if (searchToke.getType() == type) {
				return searchToke;
			}
		}
		return null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
