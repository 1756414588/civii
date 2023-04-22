package com.game.timer;

import com.game.service.FishingService;
import com.game.spring.SpringUtil;

/**
 * 渔场定时器
 */
public class FishingQueueTimer extends TimerEvent {

	public FishingQueueTimer() {
		super(-1, 10000);
	}

	@Override
	public void action() {
		SpringUtil.getBean(FishingService.class).checkDispatchQueue();
	}

}