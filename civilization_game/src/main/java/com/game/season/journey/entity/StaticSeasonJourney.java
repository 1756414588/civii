package com.game.season.journey.entity;

import java.util.List;

public class StaticSeasonJourney {

	private int id;
	private int taskId;
	private int type;
	private int cond;
	private String desc;
	private List<List<Integer>> award;
	private int awardId;
	private List<Integer> heroId;
	private int isIniTask;
	private int nextTask;



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<List<Integer>> getAward() {
		return award;
	}

	public void setAward(List<List<Integer>> award) {
		this.award = award;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public List<Integer> getHeroId() {
		return heroId;
	}

	public void setHeroId(List<Integer> heroId) {
		this.heroId = heroId;
	}

	public int getIsIniTask() {
		return isIniTask;
	}

	public void setIsIniTask(int isIniTask) {
		this.isIniTask = isIniTask;
	}

	public int getNextTask() {
		return nextTask;
	}

	public void setNextTask(int nextTask) {
		this.nextTask = nextTask;
	}
}
