package com.game.domain.s;

import java.util.List;

// 世界地图怪物比例表
public class StaticMonsterNum {
    private int id;
	private int mapType;
	private List<List<Integer>> monsters;
	private int maxNum;
	private int worldTarget;
	public int getMapType() {
		return mapType;
	}

	public void setMapType(int mapType) {
		this.mapType = mapType;
	}

	public List<List<Integer>> getMonsters() {
		return monsters;
	}

	public void setMonsters(List<List<Integer>> monsters) {
		this.monsters = monsters;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

	public int getWorldTarget() {
		return worldTarget;
	}

	public void setWorldTarget(int worldTarget) {
		this.worldTarget = worldTarget;
	}
}
