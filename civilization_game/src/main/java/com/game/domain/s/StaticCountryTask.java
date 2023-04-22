package com.game.domain.s;

import java.util.List;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-3-13 下午2:02:51
 * @describe
 */
public class StaticCountryTask {
	private int taskId;
	private int type;
	private int cond;
	private List<List<Integer>> awardList;
	private int countryLv;
	private  List<Integer> extraAward;

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

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

    public int getCountryLv() {
        return countryLv;
    }

    public void setCountryLv(int countryLv) {
        this.countryLv = countryLv;
    }

	public List<Integer> getExtraAward() {
		return extraAward;
	}

	public void setExtraAward(List<Integer> extraAward) {
		this.extraAward = extraAward;
	}
}
