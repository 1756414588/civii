package com.game.domain.p;

import java.util.Date;

/**
 * @filename
 * @author 陈奎
 * @version 1.0
 * @time 2017-7-24 上午10:09:05
 * @describe
 */
public class Suggest {

	private long lordId;
	private int level;
	private String content;
	private Date sendTime;

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

}
