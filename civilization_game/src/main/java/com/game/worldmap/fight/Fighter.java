package com.game.worldmap.fight;

import com.game.worldmap.March;
import com.game.worldmap.Pos;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 基础战斗的对像
 */
public class Fighter implements IFighter {

	// 编号
	private long id;
	// 名称
	private String name;
	// 国家
	private int country;
	// 等级
	private int level;
	// 坐标点
	private Pos pos;
	// 战力
	private long power;
	// 求援次数
	private int helpTime;
	// 玩家类型0 玩家 1 城池,2怪物
	private int type;
	// 行军
	ConcurrentLinkedDeque<March> marchList = new ConcurrentLinkedDeque<>();

	private int soilder;

	public Fighter() {
	}

	public Fighter(long id, int type, int country, Pos pos) {
		this.id = id;
		this.type = type;
		this.country = country;
		this.pos = pos;
	}


	public Fighter(long id, int type, int country, int helpTime, Pos pos) {
		this.id = id;
		this.type = type;
		this.country = country;
		this.helpTime = helpTime;
		this.pos = pos;
	}

	public Fighter(long id, String name, int country, int level, long power, Pos pos) {
		this.id = id;
		this.name = name;
		this.country = country;
		this.level = level;
		this.power = power;
		this.pos = pos;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public Pos getPos() {
		return pos;
	}

	@Override
	public int getCountry() {
		return country;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public ConcurrentLinkedDeque<March> getMarchList() {
		return marchList;
	}

	@Override
	public void addMarch(March march) {
		if (!marchList.contains(march)) {
			marchList.add(march);
		}
	}

	@Override
	public boolean removeMarch(March march) {
		return marchList.remove(march);
	}

	@Override
	public int getHelpTime() {
		return helpTime;
	}

	@Override
	public int getSoldierNum() {
		return soilder;
	}

	public void setHelpTime(int helpTime) {
		this.helpTime = helpTime;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setSoilder(int soilder) {
		this.soilder = soilder;
	}


}
