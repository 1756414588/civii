package com.game.domain.s;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-4-21 下午7:33:20
 * @describe
 */
public class StaticChat {

	private int chatId;
	private int type;
	private int limitLevel;

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLimitLevel() {
		return limitLevel;
	}

	public void setLimitLevel(int limitLevel) {
		this.limitLevel = limitLevel;
	}
}
