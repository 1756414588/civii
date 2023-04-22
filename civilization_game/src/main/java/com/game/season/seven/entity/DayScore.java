package com.game.season.seven.entity;

import com.game.domain.Player;

public class DayScore {

	private Player player;

	private int day;

	private int score;

	public DayScore() {

	}

	public DayScore(Player player, int day, int score) {
		this.player = player;
		this.day = day;
		this.score = score;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void addScore(int score) {
		this.score += score;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player)) {
			return false;
		}
		Player pairo = (Player) o;
		return getPlayer() == pairo;
	}
}
