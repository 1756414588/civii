package com.game.season.seven.entity;

import java.util.List;

public class StaticSeasonSevenType {

	private int id;
	private int type;
	private int taskType;
	private int time;
	private int limitTime;
	private int getScore;
	private int jumpType;
	private int compseasonNum;
	private String desc;
	private String taskCond;
	private int condType;
	private int cond;
	private List<Integer> section;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTaskType() {
		return taskType;
	}

	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(int limitTime) {
		this.limitTime = limitTime;
	}

	public int getGetScore() {
		return getScore;
	}

	public void setGetScore(int getScore) {
		this.getScore = getScore;
	}

	public int getJumpType() {
		return jumpType;
	}

	public void setJumpType(int jumpType) {
		this.jumpType = jumpType;
	}

	public int getCompseasonNum() {
		return compseasonNum;
	}

	public void setCompseasonNum(int compseasonNum) {
		this.compseasonNum = compseasonNum;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTaskCond() {
		return taskCond;
	}

	public void setTaskCond(String taskCond) {
		this.taskCond = taskCond;
	}

	public int getCondType() {
		return condType;
	}

	public void setCondType(int condType) {
		this.condType = condType;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public List<Integer> getSection() {
		return section;
	}

	public void setSection(List<Integer> section) {
		this.section = section;
	}
}
