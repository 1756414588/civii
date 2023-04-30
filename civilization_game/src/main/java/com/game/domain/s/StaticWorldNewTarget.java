package com.game.domain.s;

import java.util.List;

/**
 *
 * @date 2019/12/23 16:54
 * @description
 */
public class StaticWorldNewTarget {

    private int targetId;
    private String title;
    private int targetType;
    private int subObject;
    private List<Integer> campRanking;
    private int worldGoal;
    private int worldGoalAward;
    private int personalGoal;
    private int personalGoalAward;
    private List<Integer> activateFuntion;
    private int nextId;

    private int worldGoal2;
    private int worldGoalAward2;
    private String worldGoalDesc2;
    private int lastTime;
    private int limitLevel;

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public int getSubObject() {
        return subObject;
    }

    public void setSubObject(int subObject) {
        this.subObject = subObject;
    }

    public List<Integer> getCampRanking() {
        return campRanking;
    }

    public void setCampRanking(List<Integer> campRanking) {
        this.campRanking = campRanking;
    }

    public int getWorldGoal() {
        return worldGoal;
    }

    public void setWorldGoal(int worldGoal) {
        this.worldGoal = worldGoal;
    }

    public int getWorldGoalAward() {
        return worldGoalAward;
    }

    public void setWorldGoalAward(int worldGoalAward) {
        this.worldGoalAward = worldGoalAward;
    }

    public int getPersonalGoal() {
        return personalGoal;
    }

    public void setPersonalGoal(int personalGoal) {
        this.personalGoal = personalGoal;
    }

    public int getPersonalGoalAward() {
        return personalGoalAward;
    }

    public void setPersonalGoalAward(int personalGoalAward) {
        this.personalGoalAward = personalGoalAward;
    }

    public List<Integer> getActivateFuntion() {
        return activateFuntion;
    }

    public void setActivateFuntion(List<Integer> activateFuntion) {
        this.activateFuntion = activateFuntion;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    public int getWorldGoal2() {
        return worldGoal2;
    }

    public void setWorldGoal2(int worldGoal2) {
        this.worldGoal2 = worldGoal2;
    }

    public int getWorldGoalAward2() {
        return worldGoalAward2;
    }

    public void setWorldGoalAward2(int worldGoalAward2) {
        this.worldGoalAward2 = worldGoalAward2;
    }

    public String getWorldGoalDesc2() {
        return worldGoalDesc2;
    }

    public void setWorldGoalDesc2(String worldGoalDesc2) {
        this.worldGoalDesc2 = worldGoalDesc2;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public int getLimitLevel() {
        return limitLevel;
    }

    public void setLimitLevel(int limitLevel) {
        this.limitLevel = limitLevel;
    }
}
