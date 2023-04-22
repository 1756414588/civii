package com.game.domain.p;

import com.game.pb.CommonPb;

public class CityRemark {

	private int country;
	private long remarkTime;
	private String msg;
	private long nextTime;
	private int remarkHour;
	private int remarkMin;
	private int cityId;

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public long getRemarkTime() {
		return remarkTime;
	}

	public void setRemarkTime(long remarkTime) {
		this.remarkTime = remarkTime;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getNextTime() {
		return nextTime;
	}

	public void setNextTime(long nextTime) {
		this.nextTime = nextTime;
	}

	public CommonPb.CityRemark encode() {
		CommonPb.CityRemark.Builder builder = CommonPb.CityRemark.newBuilder();
		builder.setRemarkTime(this.remarkTime);
		builder.setRemark(this.msg);
		builder.setCountry(this.country);
		builder.setNextRemarkTime(this.nextTime);
		builder.setRemarkHour(this.remarkHour);
		builder.setRemarkMin(this.remarkMin);
		builder.setCityId(this.cityId);
		return builder.build();
	}

	public void decode(CommonPb.CityRemark builder) {
		this.remarkTime = builder.getRemarkTime();
		this.country = builder.getCountry();
		this.nextTime = builder.getNextRemarkTime();
		this.msg = builder.getRemark();
		this.remarkHour = builder.getRemarkHour();
		this.remarkMin = builder.getRemarkMin();
		this.cityId = builder.getCityId();
	}

	public int getRemarkHour() {
		return remarkHour;
	}

	public void setRemarkHour(int remarkHour) {
		this.remarkHour = remarkHour;
	}

	public int getRemarkMin() {
		return remarkMin;
	}

	public void setRemarkMin(int remarkMin) {
		this.remarkMin = remarkMin;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
}
