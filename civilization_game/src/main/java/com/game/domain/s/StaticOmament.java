package com.game.domain.s;

import java.util.List;

/**
 * 2020年8月5日
 * 
 * @CaoBing halo_game StaticOmament.java
 *
 *          配饰的配置实体类
 **/
public class StaticOmament {
	private int id; // 配饰的唯一id
	private String name; // 配饰的名字
	private int type; // 配饰的类型：1为攻击配饰，为防御配饰，3为兵力配饰
	private int level; // 每个配饰的等级
	private List<Integer> property; // 配饰的加成属性，和类型组合决定加成什么属性，加成多少
	private int composeId; // 合成的下一级配饰
	private int needNum; // 合成下一级配饰所需数量
	private int quality; // 品质

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

	public List<Integer> getProperty() {
		return property;
	}

	public void setProperty(List<Integer> property) {
		this.property = property;
	}

	public int getComposeId() {
		return composeId;
	}

	public void setComposeId(int composeId) {
		this.composeId = composeId;
	}

	public int getNeedNum() {
		return needNum;
	}

	public void setNeedNum(int needNum) {
		this.needNum = needNum;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	@Override
	public String toString() {
		return "StaticOmament [id=" + id + ", name=" + name + ", type=" + type + ", level=" + level + ", property=" + property + ", composeId=" + composeId
				+ ", needNum=" + needNum + ", quality=" + quality + "]";
	}
}
