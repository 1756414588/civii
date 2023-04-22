package com.game.domain.s;

/**
 * @author cpz
 * @date 2020/8/19 15:02
 * @description
 */
public class StaticTowerWarBonus {

	private int id;
	private int type;
	private int level;
	private int valve;
	private int effect;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getValve() {
		return valve;
	}

	public void setValve(int valve) {
		this.valve = valve;
	}

	public int getEffect() {
		return effect;
	}

	public void setEffect(int effect) {
		this.effect = effect;
	}

	@Override
	public String toString() {
		return "StaticTowerWarBonus{" + "id=" + id + ", type=" + type + ", level=" + level + ", valve=" + valve + ", effect=" + effect + '}';
	}
}
