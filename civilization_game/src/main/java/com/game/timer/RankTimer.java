package com.game.timer;

import com.game.service.CountryService;
import com.game.spring.SpringUtil;

public class RankTimer extends TimerEvent {
	public RankTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		SpringUtil.getBean(CountryService.class).timerCountryLogic();
	}

}