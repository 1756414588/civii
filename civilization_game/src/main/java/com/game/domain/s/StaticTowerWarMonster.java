package com.game.domain.s;

import com.game.pb.CommonPb;

/**
 * @author cpz
 * @date 2020/8/19 15:02
 * @description
 */
public class StaticTowerWarMonster {
	private int id;
	private String name;
	private String icon;
	private String model;
	private int hp;
	private int defence;
	private int speed;
	private int damage;
	private int award;
	private int scale;
	private String soundDeath;
	private int viewLayer;

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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public int getDefence() {
		return defence;
	}

	public void setDefence(int defence) {
		this.defence = defence;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getAward() {
		return award;
	}

	public void setAward(int award) {
		this.award = award;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getSoundDeath() {
		return soundDeath;
	}

	public void setSoundDeath(String soundDeath) {
		this.soundDeath = soundDeath;
	}

	public int getViewLayer() {
		return viewLayer;
	}

	public void setViewLayer(int viewLayer) {
		this.viewLayer = viewLayer;
	}

	public CommonPb.Monster.Builder wrapPb() {
		return CommonPb.Monster.newBuilder().setId(getId()).setName(getName()).setModel(getModel()).setHp(getHp()).setDefence(getDefence()).setSpeed(getSpeed()).setDamage(getDamage()).setAddSupplies(getAward()).setScale(getScale());
	}

	@Override
	public StaticTowerWarMonster clone() {
		StaticTowerWarMonster staticTowerWarMonster = null;
		try {
			staticTowerWarMonster = (StaticTowerWarMonster) super.clone();

		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return staticTowerWarMonster;
	}
}
