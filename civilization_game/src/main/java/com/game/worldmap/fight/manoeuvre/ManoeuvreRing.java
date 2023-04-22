package com.game.worldmap.fight.manoeuvre;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManoeuvreRing {

	private long openTime;
	private int status;
	private int planState;
	private long startTime;
	private long endTime;

	public ManoeuvreRing(int planState, int status, long openTime, long startTime, long endTime) {
		this.planState = planState;
		this.status = status;
		this.openTime = openTime;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
