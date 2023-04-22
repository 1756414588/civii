package com.game.flame;

import com.game.domain.Player;
import com.game.worldmap.March;

public class FlameGuard {

	private March march; // 部队信息
	private long startTime;// 开始的采集的时间
	private FlameWarResource resouce;
	private long nextCalTime;
	private long totalRes;
	private Player player;

	public March getMarch() {
		return march;
	}

	public void setMarch(March march) {
		this.march = march;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public FlameWarResource getResouce() {
		return resouce;
	}

	public void setResouce(FlameWarResource resouce) {
		this.resouce = resouce;
	}

	public long getNextCalTime() {
		return nextCalTime;
	}

	public void setNextCalTime(long nextCalTime) {
		this.nextCalTime = nextCalTime;
	}

	public long getTotalRes() {
		return totalRes;
	}

	public void setTotalRes(long totalRes) {
		this.totalRes = totalRes;
	}

	public void addTotalRes(long res) {
		this.totalRes += res;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}
