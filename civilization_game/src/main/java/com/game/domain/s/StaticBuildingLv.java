package com.game.domain.s;

import java.util.List;

public class StaticBuildingLv {
    private int keyId;
	private int buildingType;
	private int level;
	private int uptime;
    private List<List<Long>> upCond;
    private List<List<Long>> resourceCond;
    private List<List<Long>> action;
    private List<Long> resourceOut;
    private int battleScore;
    private List<Integer> award;
    private List<Integer> beautyAward;

    public int getKeyId () {
        return keyId;
    }

    public void setKeyId (int keyId) {
        this.keyId = keyId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getUptime () {
        return uptime;
    }

    public void setUptime (int uptime) {
        this.uptime = uptime;
    }


    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public List<List<Long>> getUpCond() {
        return upCond;
    }

    public void setUpCond(List<List<Long>> upCond) {
        this.upCond = upCond;
    }

    public List<List<Long>> getResourceCond() {
        return resourceCond;
    }

    public void setResourceCond(List<List<Long>> resourceCond) {
        this.resourceCond = resourceCond;
    }

    public List<List<Long>> getAction() {
        return action;
    }

    public void setAction(List<List<Long>> action) {
        this.action = action;
    }

    public List<Long> getResourceOut() {
        return resourceOut;
    }

    public void setResourceOut(List<Long> resourceOut) {
        this.resourceOut = resourceOut;
    }

    public int getBattleScore() {
        return battleScore;
    }

    public void setBattleScore(int battleScore) {
        this.battleScore = battleScore;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

	public List<Integer> getBeautyAward() {
		return beautyAward;
	}

	public void setBeautyAward(List<Integer> beautyAward) {
		this.beautyAward = beautyAward;
	}
}
