package com.game.season.hero;

import java.util.List;

public class StaticComSkill {
	private int id;
	private int skillLv;
	private int pr;

	private int firstDamage;
	private int times;
	private int leftDamage;
	private int totalDamage;
	private List<List<Integer>> upgrade;
	private int type;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSkillLv() {
		return skillLv;
	}

	public void setSkillLv(int skillLv) {
		this.skillLv = skillLv;
	}

	public int getPr() {
		return pr;
	}

	public void setPr(int pr) {
		this.pr = pr;
	}

	public int getFirstDamage() {
		return firstDamage;
	}

	public void setFirstDamage(int firstDamage) {
		this.firstDamage = firstDamage;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getLeftDamage() {
		return leftDamage;
	}

	public void setLeftDamage(int leftDamage) {
		this.leftDamage = leftDamage;
	}

	public int getTotalDamage() {
		return totalDamage;
	}

	public void setTotalDamage(int totalDamage) {
		this.totalDamage = totalDamage;
	}

	public List<List<Integer>> getUpgrade() {
		return upgrade;
	}

	public void setUpgrade(List<List<Integer>> upgrade) {
		this.upgrade = upgrade;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
