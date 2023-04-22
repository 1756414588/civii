package com.game.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/8 10:44
 **/
public class StaticEndlessLevel {
	private int id;
	private String name;
	private int wave_count;
	private Map<Integer, Map<Integer, List<Integer>>> wave_list;
	private Map<Integer, List<Integer>> way_list;
	private List<List<Integer>> drop;
	private List<List<Integer>> betterdrop;
	private int timeLimit;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWave_count() {
		return wave_count;
	}

	public void setWave_count(int wave_count) {
		this.wave_count = wave_count;
	}

	public Map<Integer, Map<Integer, List<Integer>>> getWave_list() {
		return wave_list;
	}

	public void setWave_list(Map<Integer, Map<Integer, List<Integer>>> wave_list) {
		this.wave_list = wave_list;
	}

	public Map<Integer, List<Integer>> getWay_list() {
		return way_list;
	}

	public void setWay_list(Map<Integer, List<Integer>> way_list) {
		this.way_list = way_list;
	}

	public List<List<Integer>> getDrop() {
		return drop;
	}

	public void setDrop(List<List<Integer>> drop) {
		this.drop = drop;
	}

	public List<List<Integer>> getBetterdrop() {
		return betterdrop;
	}

	public void setBetterdrop(List<List<Integer>> betterdrop) {
		this.betterdrop = betterdrop;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	@Override
	public String toString() {
		return "StaticEndlessLevel{" +
			"id=" + id +
			", name='" + name + '\'' +
			", wave_count=" + wave_count +
			", wave_list=" + wave_list +
			", way_list=" + way_list +
			", drop=" + drop +
			", betterdrop=" + betterdrop +
			", timeLimit=" + timeLimit +
			'}';
	}
}
