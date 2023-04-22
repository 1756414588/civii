package com.game.timer;

import com.game.activity.ActivityEventManager;
import com.game.activity.actor.TimeDisappearActor;
import com.game.activity.define.EventEnum;
import com.game.util.TimeHelper;

/**
 * 活动关闭通知
 */
public class ActivityCloseTimer extends TimerEvent {

	public ActivityCloseTimer() {
		super(-1, TimeHelper.THREE_SECOND_MS);
	}

	@Override
	public void action() {
		long now = System.currentTimeMillis();
		ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new TimeDisappearActor(now));
	}
}
