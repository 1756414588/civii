package com.game.domain.s;

import java.util.List;

public class StaticZergMonster {

	private int keyId;
	private int type;
	private int waves;
	private List<Integer> monsters;
	private int showId;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getWaves() {
		return waves;
	}

	public void setWaves(int waves) {
		this.waves = waves;
	}

	public List<Integer> getMonsters() {
		return monsters;
	}

	public void setMonsters(List<Integer> monsters) {
		this.monsters = monsters;
	}

	public int getShowId() {
		return showId;
	}

	public void setShowId(int showId) {
		this.showId = showId;
	}
}
