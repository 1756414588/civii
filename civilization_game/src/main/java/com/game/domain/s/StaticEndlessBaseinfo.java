package com.game.domain.s;

import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/7 14:07
 **/
public class StaticEndlessBaseinfo {
	private int id;
	private String resource;
	private List<Integer> condition;
	private int life_point;
	private int base_supplies;
	private List<List<Integer>> tower_list;
	private List<Integer> monster_list;
	private List<List<Double>> way_point_list;
	private String zerg_startaxis;
	private List<List<Double>> tower_base_list;
	private List<Integer> camera_limit;
	private List<Integer> camera_init;
	private List<List<Integer>> level_zergbuff;
	private List<Integer> level_loop;
	private int coin_reward;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public List<Integer> getCondition() {
		return condition;
	}

	public void setCondition(List<Integer> condition) {
		this.condition = condition;
	}

	public int getLife_point() {
		return life_point;
	}

	public void setLife_point(int life_point) {
		this.life_point = life_point;
	}

	public int getBase_supplies() {
		return base_supplies;
	}

	public void setBase_supplies(int base_supplies) {
		this.base_supplies = base_supplies;
	}

	public List<List<Integer>> getTower_list() {
		return tower_list;
	}

	public void setTower_list(List<List<Integer>> tower_list) {
		this.tower_list = tower_list;
	}

	public List<Integer> getMonster_list() {
		return monster_list;
	}

	public void setMonster_list(List<Integer> monster_list) {
		this.monster_list = monster_list;
	}

	public List<List<Double>> getWay_point_list() {
		return way_point_list;
	}

	public void setWay_point_list(List<List<Double>> way_point_list) {
		this.way_point_list = way_point_list;
	}

	public String getZerg_startaxis() {
		return zerg_startaxis;
	}

	public void setZerg_startaxis(String zerg_startaxis) {
		this.zerg_startaxis = zerg_startaxis;
	}

	public List<List<Double>> getTower_base_list() {
		return tower_base_list;
	}

	public void setTower_base_list(List<List<Double>> tower_base_list) {
		this.tower_base_list = tower_base_list;
	}

	public List<Integer> getCamera_limit() {
		return camera_limit;
	}

	public void setCamera_limit(List<Integer> camera_limit) {
		this.camera_limit = camera_limit;
	}

	public List<Integer> getCamera_init() {
		return camera_init;
	}

	public void setCamera_init(List<Integer> camera_init) {
		this.camera_init = camera_init;
	}

	public List<List<Integer>> getLevel_zergbuff() {
		return level_zergbuff;
	}

	public void setLevel_zergbuff(List<List<Integer>> level_zergbuff) {
		this.level_zergbuff = level_zergbuff;
	}

	public List<Integer> getLevel_loop() {
		return level_loop;
	}

	public void setLevel_loop(List<Integer> level_loop) {
		this.level_loop = level_loop;
	}

	public int getCoin_reward() {
		return coin_reward;
	}

	public void setCoin_reward(int coin_reward) {
		this.coin_reward = coin_reward;
	}

	@Override
	public String toString() {
		return "StaticEndlessBaseinfo{" +
			"id=" + id +
			", resource='" + resource + '\'' +
			", condition=" + condition +
			", life_point=" + life_point +
			", base_supplies=" + base_supplies +
			", tower_list=" + tower_list +
			", monster_list=" + monster_list +
			", way_point_list=" + way_point_list +
			", zerg_startaxis='" + zerg_startaxis + '\'' +
			", tower_base_list=" + tower_base_list +
			", camera_limit=" + camera_limit +
			", camera_init=" + camera_init +
			", level_zergbuff=" + level_zergbuff +
			", level_loop=" + level_loop +
			", coin_reward='" + coin_reward + '\'' +
			'}';
	}
}
