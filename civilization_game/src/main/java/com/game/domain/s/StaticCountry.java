package com.game.domain.s;

import java.util.List;

/**
 * @Author 陈奎
 * @Description 阵营的加成
 * @Date 2023/4/10 15:22
 **/

public class StaticCountry {

	private int countryId;
	private String name;
	private List<List<Integer>> resouceAddtion;
	private List<List<Integer>> sodierAddtion;

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<List<Integer>> getResouceAddtion() {
		return resouceAddtion;
	}

	public void setResouceAddtion(List<List<Integer>> resouceAddtion) {
		this.resouceAddtion = resouceAddtion;
	}

	public List<List<Integer>> getSodierAddtion() {
		return sodierAddtion;
	}

	public void setSodierAddtion(List<List<Integer>> sodierAddtion) {
		this.sodierAddtion = sodierAddtion;
	}
}
