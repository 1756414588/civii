package com.game.timer;

import com.game.service.ActivityService;
import com.game.spring.SpringUtil;

/**
 *
 * @version 1.0
 * @filename
 * @time 2017-5-20 下午6:25:00
 * @describe
 */
public class ActivityTimer extends TimerEvent {

	public ActivityTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		ActivityService service = SpringUtil.getBean(ActivityService.class);
		service.activityTimerLogic();
		//service.activityRewardLogic();
	}

}
