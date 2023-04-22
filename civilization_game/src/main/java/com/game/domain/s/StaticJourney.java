package com.game.domain.s;

import java.util.List;

/**
*2020年8月17日
*@CaoBing
*halo_game
*StaticJourney.java
**/
public class StaticJourney {
	private int journeyId; // 关卡ID
	private int nextJourney; // 开启的下一关卡ID
	private int mapId; // 章节ID
	private String name; // 关卡名称
	private int journeyType; // 关卡类型 1普通关卡 2高级关卡
	private List<Integer> monsterIds;
	private int winCost; // 成功消耗的次数
	private int failedCost; // 失败消耗的次数
	private List<Integer> stableAwards; // 固定配饰奖励[奖励类型,配饰ID,配饰数量]

	/**
	 * //随机配饰奖励 [掉落0个的概率,掉落1个的概率,掉落2个的概率,掉落3个的概率,掉落4个的概率...] a.配饰掉落概率：该数量配饰掉落概率/100
	 * b.配饰掉落数量=索引值 c.配饰掉落类型为三种配饰中随机一种
	 */
	private List<Integer> randomAwards;
	
	private List<List<Integer>> randomAwardId; 

	public int getJourneyId() {
		return journeyId;
	}

	public void setJourneyId(int journeyId) {
		this.journeyId = journeyId;
	}

	public int getNextJourney() {
		return nextJourney;
	}

	public void setNextJourney(int nextJourney) {
		this.nextJourney = nextJourney;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getJourneyType() {
		return journeyType;
	}

	public void setJourneyType(int journeyType) {
		this.journeyType = journeyType;
	}

	public List<Integer> getMonsterIds() {
		return monsterIds;
	}

	public void setMonsterIds(List<Integer> monsterIds) {
		this.monsterIds = monsterIds;
	}

	public int getWinCost() {
		return winCost;
	}

	public void setWinCost(int winCost) {
		this.winCost = winCost;
	}

	public int getFailedCost() {
		return failedCost;
	}

	public void setFailedCost(int failedCost) {
		this.failedCost = failedCost;
	}

	public List<Integer> getStableAwards() {
		return stableAwards;
	}

	public void setStableAwards(List<Integer> stableAwards) {
		this.stableAwards = stableAwards;
	}

	public List<Integer> getRandomAwards() {
		return randomAwards;
	}

	public void setRandomAwards(List<Integer> randomAwards) {
		this.randomAwards = randomAwards;
	}

	public List<List<Integer>> getRandomAwardId() {
		return randomAwardId;
	}

	public void setRandomAwardId(List<List<Integer>> randomAwardId) {
		this.randomAwardId = randomAwardId;
	}

	@Override
	public String toString() {
		return "StaticJourney [journeyId=" + journeyId + ", nextJourney=" + nextJourney + ", mapId=" + mapId + ", name=" + name + ", journeyType=" + journeyType
				+ ", monsterIds=" + monsterIds + ", winCost=" + winCost + ", failedCost=" + failedCost + ", stableAwards=" + stableAwards + ", randomAwards="
				+ randomAwards + ", randomAwardId=" + randomAwardId + "]";
	}
}
