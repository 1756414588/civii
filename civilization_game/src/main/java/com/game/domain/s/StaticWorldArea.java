package com.game.domain.s;

import java.util.List;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-2-21 下午4:21:41
 * @describe
 */
public class StaticWorldArea {

	private int areaId;
	private int sceneId;
	private List<List<Integer>> resource;
	private int register;
	private int worldLv;
	private int x;
	private int y;

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public int getSceneId() {
		return sceneId;
	}

	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}

	public List<List<Integer>> getResource() {
		return resource;
	}

	public void setResource(List<List<Integer>> resource) {
		this.resource = resource;
	}

	public int getRegister() {
		return register;
	}

	public void setRegister(int register) {
		this.register = register;
	}

	public int getWorldLv() {
		return worldLv;
	}

	public void setWorldLv(int worldLv) {
		this.worldLv = worldLv;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}
