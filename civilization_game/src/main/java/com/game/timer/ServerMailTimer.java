package com.game.timer;

import com.game.service.MailService;
import com.game.spring.SpringUtil;

public class ServerMailTimer extends TimerEvent {
	public ServerMailTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		SpringUtil.getBean(MailService.class).sendTimerLogic();
	}
}
