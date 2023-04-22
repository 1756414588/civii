package com.game.domain.p;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-3-13 上午11:39:53
 * @describe 国家选举
 */
public class CtyVote {

	private long lordId;
	private int vote;

	public CtyVote(long lordId) {
		this.lordId = lordId;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public int getVote() {
		return vote;
	}

	public void setVote(int vote) {
		this.vote = vote;
	}

}
