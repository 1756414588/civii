package com.game.domain.s;

import java.util.List;

import com.game.pb.CommonPb;

public class StaticActAward {

	private int keyId;
	private int awardId;
	private int sortId;
	private int cond;
	private List<List<Integer>> awardList;
	private String param;
	private List<CommonPb.Award> awardPbList;
	private String desc;	//描述

	private int cost;
	private List<Integer> paramList;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getSortId() {
		return sortId;
	}

	public void setSortId(int sortId) {
		this.sortId = sortId;
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

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public List<CommonPb.Award> getAwardPbList() {
		return awardPbList;
	}

	public void setAwardPbList(List<CommonPb.Award> awardPbList) {
		this.awardPbList = awardPbList;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public List<Integer> getParamList() {
		return paramList;
	}

	public void setParamList(List<Integer> paramList) {
		this.paramList = paramList;
	}
}
