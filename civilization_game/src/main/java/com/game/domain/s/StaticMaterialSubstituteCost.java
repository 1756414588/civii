package com.game.domain.s;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/27 15:54
 **/
public class StaticMaterialSubstituteCost {
	private int id;
	private int times;
	private int cost;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return "StaticMaterialSubstituteCost{" + "id=" + id + ", times=" + times + ", cost=" + cost + '}';
	}
}
