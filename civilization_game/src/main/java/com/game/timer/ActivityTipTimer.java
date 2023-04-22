package com.game.timer;

import com.game.activity.ActTipManager;
import com.game.util.TimeHelper;

/**
 * 定时推送活动红点信息
 */
public class ActivityTipTimer extends TimerEvent {

	public ActivityTipTimer() {
		super(-1, 3 * TimeHelper.SECOND_MS);
	}

	@Override
	public void action() {
		ActTipManager.getInst().pushTip();
	}
}
