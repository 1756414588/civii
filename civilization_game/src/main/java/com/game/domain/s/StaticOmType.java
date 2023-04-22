package com.game.domain.s;

/**
 * 2020年8月5日
 * 
 * @CaoBing halo_game StaticOmType.java
 *
 *          配饰类型的配置实体类
 **/
public class StaticOmType {
	private int id; // 配饰位的唯一Id
	private String name; // 配饰位的名字
	private int type; // 配饰位的解锁等级
	private int level; // 每个配饰的等级
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
	@Override
	public String toString() {
		return "StaticOmType [id=" + id + ", name=" + name + ", type=" + type + ", level=" + level + "]";
	}
}
