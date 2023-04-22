package com.game.timer;

import com.game.define.AppTimer;
import com.game.manager.MessageEventManager;
import com.game.spring.SpringUtil;


@AppTimer(desc = "机器人行为")
public class RobotOpratorTimer extends TimerEvent {

	public RobotOpratorTimer() {
		super(-1, 300);
	}

	@Override
	public void action() {
		MessageEventManager actionManager = SpringUtil.getBean(MessageEventManager.class);
		actionManager.actionTimer();
	}
}
