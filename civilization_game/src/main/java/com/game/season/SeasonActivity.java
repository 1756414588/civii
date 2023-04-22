package com.game.season;

import com.game.pb.SeasonActivityPb;

public class SeasonActivity {

	private int id;

	private int actId;

	/**
	 * 开启的时间
	 */
	private long openTime;

	/**
	 * 预热开始时间
	 */
	private long preheatTime;
	/**
	 * 结束时间
	 */
	private long endTime;

	/**
	 * 0 未开启 1 预热 2 开始 3 结束 4展示
	 */
	private SeasonState state;

	private long exhibitionTime;// 展示结束时间

	private int awardId;

	public SeasonActivity() {

	}

	public SeasonActivity(SeasonActivityPb.SeasonActInfo info) {
		this.id = info.getId();
		this.actId = info.getActId();
		this.openTime = info.getOpenTime();
		this.preheatTime = info.getPreheatTime();
		this.endTime = info.getEndTime();
		this.state = SeasonState.getSeasonState(info.getState());
		this.exhibitionTime = info.getExhibitionTime();
		this.awardId = info.getAwardId();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	public long getPreheatTime() {
		return preheatTime;
	}

	public void setPreheatTime(long preheatTime) {
		this.preheatTime = preheatTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public SeasonState getState() {
		return state;
	}

	public void setState(SeasonState state) {
		this.state = state;
	}

	public long getExhibitionTime() {
		return exhibitionTime;
	}

	public void setExhibitionTime(long exhibitionTime) {
		this.exhibitionTime = exhibitionTime;
	}

	public int getActId() {
		return actId;
	}

	public void setActId(int actId) {
		this.actId = actId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public SeasonActivityPb.SeasonActInfo encode() {
		SeasonActivityPb.SeasonActInfo.Builder builder = SeasonActivityPb.SeasonActInfo.newBuilder();
		builder.setId(this.id);
		builder.setActId(this.actId);
		builder.setOpenTime(this.openTime);
		builder.setPreheatTime(this.preheatTime);
		builder.setEndTime(this.endTime);
		builder.setState(this.state.getState());
		builder.setExhibitionTime(this.exhibitionTime);
		builder.setAwardId(this.awardId);
		return builder.build();

	}

	@Override
	public String toString() {
		return "SeasonActivity{" + "id=" + id + ", openTime=" + openTime + ", preheatTime=" + preheatTime + ", endTime=" + endTime + ", state=" + state + ", exhibitionTime=" + exhibitionTime + '}';
	}
}
