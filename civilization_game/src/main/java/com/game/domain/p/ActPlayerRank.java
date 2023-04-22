package com.game.domain.p;

public class ActPlayerRank {
	private long lordId;
	private String nick;
	private long rankValue;// 值
	private int rank;
	private long time;

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public long getRankValue() {
		return rankValue;
	}

	public void setRankValue(long rankValue) {
		this.rankValue = rankValue;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public ActPlayerRank() {
	}

	public ActPlayerRank(long lordId, long value,long time) {
		this.lordId = lordId;
		this.rankValue = value;
		this.time = time;
	}

}
