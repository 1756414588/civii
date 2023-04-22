package com.game.worldmap.fight.brood;

import com.game.domain.p.SquareMonster;
import com.game.worldmap.fight.Fighter;
import java.util.HashMap;
import java.util.Map;

/**
 * 近卫军
 */


public class BroodFighter extends Fighter {

	private int cityId;        //国战城池ID
	private String cityName;//禁卫军出动的时候 需要四方要塞的名字
	private Map<Integer, SquareMonster> monsters = new HashMap<Integer, SquareMonster>();

	public BroodFighter() {
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public Map<Integer, SquareMonster> getMonsters() {
		return monsters;
	}

	public void setMonsters(Map<Integer, SquareMonster> monsters) {
		this.monsters = monsters;
	}
}
