package com.game.domain.p;

import com.game.pb.DataPb;

/**
 * 排行榜比较器
 */
public class CtyRank implements Comparable<CtyRank> {
	private long lordId;
	private int v;
	private int rank;
	private long time;

	public CtyRank(long lordId, int v, long time) {
		this.lordId = lordId;
		this.v = v;
		this.time = time;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
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

	public CtyRank() {
	}

	public CtyRank(DataPb.CtyRank ctyRank) {
		this.lordId = ctyRank.getLordId();
		this.v = ctyRank.getV();
		this.rank = ctyRank.getRank();
		this.time = ctyRank.getTime();
	}

	public DataPb.CtyRank ser() {
		DataPb.CtyRank.Builder builder = DataPb.CtyRank.newBuilder();
		builder.setLordId(lordId);
		builder.setV(v);
		builder.setRank(rank);
		builder.setTime(time);
		return builder.build();
	}

	@Override
	public int compareTo(CtyRank o) {
		if (this.getV() > o.getV()) {
			return 1;
		} else if (this.getV() < o.getV()) {
			return -1;
		}
		return 0;
	}
}
