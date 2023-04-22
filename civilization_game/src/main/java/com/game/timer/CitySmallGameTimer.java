package com.game.timer;

import com.game.service.CityGameService;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 主城小游戏
 */
public class CitySmallGameTimer extends TimerEvent {

	public CitySmallGameTimer() {
		super(-1, TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(CityGameService.class).refush();
	}

}
