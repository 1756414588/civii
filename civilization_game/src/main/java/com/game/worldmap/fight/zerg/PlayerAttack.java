package com.game.worldmap.fight.zerg;

import com.game.worldmap.PlayerCity;
import com.game.worldmap.Pos;

public class PlayerAttack {

	private Pos pos;
	private int country;
	private PlayerCity playerCity;

	public PlayerAttack(Pos pos, PlayerCity playerCity) {
		this.pos = pos;
		this.playerCity = playerCity;
		this.country = playerCity.getCountry();
	}

	public Pos getPos() {
		return pos;
	}

	public void setPos(Pos pos) {
		this.pos = pos;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public PlayerCity getPlayerCity() {
		return playerCity;
	}

	public void setPlayerCity(PlayerCity playerCity) {
		this.playerCity = playerCity;
	}
}
