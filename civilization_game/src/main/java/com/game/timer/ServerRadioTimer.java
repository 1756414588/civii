package com.game.timer;

import com.game.service.ServerRadioService;
import com.game.spring.SpringUtil;

public class ServerRadioTimer extends TimerEvent {

	public ServerRadioTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		SpringUtil.getBean(ServerRadioService.class).serverRadioTimerLogic();
	}

}
