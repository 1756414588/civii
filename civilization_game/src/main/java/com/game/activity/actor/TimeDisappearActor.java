package com.game.activity.actor;

import com.game.activity.BaseActivityActor;

public class TimeDisappearActor extends BaseActivityActor {

	private long time;

	public TimeDisappearActor(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}
}
