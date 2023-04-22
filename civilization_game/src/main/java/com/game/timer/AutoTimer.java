package com.game.timer;

import com.game.service.AutoService;
import com.game.service.BuildingService;
import com.game.spring.SpringUtil;

public class AutoTimer extends TimerEvent {
	public AutoTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		SpringUtil.getBean(BuildingService.class).checkAuto();
		SpringUtil.getBean(AutoService.class).autoKill();
	}

}