package com.game.domain.s;

import java.util.List;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-3-13 下午2:02:51
 * @describe
 */
public class StaticCountryGlory {

	private int gloryId;
	private int level;
	private int builds;
	private int cityFight;
	private int stateFight;
	private List<List<Integer>> awardList;

	public int getGloryId() {
		return gloryId;
	}

	public void setGloryId(int gloryId) {
		this.gloryId = gloryId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBuilds() {
		return builds;
	}

	public void setBuilds(int builds) {
		this.builds = builds;
	}

	public int getCityFight() {
		return cityFight;
	}

	public void setCityFight(int cityFight) {
		this.cityFight = cityFight;
	}

	public int getStateFight() {
		return stateFight;
	}

	public void setStateFight(int stateFight) {
		this.stateFight = stateFight;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

}
