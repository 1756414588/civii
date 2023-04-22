package com.game.domain.s;

import java.util.Date;

/**
 * @author ChenKui
 * @version 创建时间：2015-12-18 下午2:47:32
 * @declare
 */

public class StaticActivityPlan {

	private int keyId;
	private int moldId;
	private int awardId;
	private int activityId;
	private int openBegin;
	private int openDays;
	private int limitDate;

	private Date beginTime;
	private Date endTime;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getMoldId() {
		return moldId;
	}

	public void setMoldId(int moldId) {
		this.moldId = moldId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getOpenBegin() {
		return openBegin;
	}

	public void setOpenBegin(int openBegin) {
		this.openBegin = openBegin;
	}

	public int getOpenDays() {
		return openDays;
	}

	public void setOpenDays(int openDays) {
		this.openDays = openDays;
	}

	public int getLimitDate() {
		return limitDate;
	}

	public void setLimitDate(int limitDate) {
		this.limitDate = limitDate;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	@Override
	public String toString() {
		return "StaticActivityPlan{" +
				"keyId=" + keyId +
				", moldId=" + moldId +
				", awardId=" + awardId +
				", activityId=" + activityId +
				", openBegin=" + openBegin +
				", openDays=" + openDays +
				", limitDate=" + limitDate +
				", beginTime=" + beginTime +
				", endTime=" + endTime +
				'}';
	}
}
