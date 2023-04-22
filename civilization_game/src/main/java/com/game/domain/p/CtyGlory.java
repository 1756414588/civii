package com.game.domain.p;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.pb.SerializePb.SerCountryGloryData;

/**
 * 国家荣誉记录
 */
public class CtyGlory {
	private AtomicInteger builds = new AtomicInteger(0);      // 建设
	private AtomicInteger cityFight = new AtomicInteger(0);   // 城战
	private AtomicInteger stateFight = new AtomicInteger(0);  // 国战
	private Set<Integer>  currentDayPush=new HashSet<>();
	private int refreshTime;

	public int getBuilds() {
		return builds.get();
	}

	public void setBuilds(int build) {
		this.builds = new AtomicInteger(build);
	}

	public int addBuild(int build) {
		return this.builds.addAndGet(build);
	}

	public int getCityFight() {
		return cityFight.get();
	}

	public void setCityFight(int cityFight) {
		this.cityFight = new AtomicInteger(cityFight);
	}

	public int addCityFight(int cityFight) {
		return this.cityFight.addAndGet(cityFight);
	}

	public int getStateFight() {
		return stateFight.get();
	}

	public void setStateFight(int stateFight) {
		this.stateFight = new AtomicInteger(stateFight);
	}

	public int addStateFight(int stateFight) {
		return this.stateFight.addAndGet(stateFight);
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}

	public Set<Integer> getCurrentDayPush() {
		return currentDayPush;
	}

	public void setCurrentDayPush(Set<Integer> currentDayPush) {
		this.currentDayPush = currentDayPush;
	}

	public void setGlory(SerCountryGloryData e) {
		this.setBuilds(e.getBuild());
		this.setCityFight(e.getCity());
		this.setStateFight(e.getState());
		this.refreshTime = e.getTime();
	}

}
