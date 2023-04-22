package com.game.message;

import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.spring.SpringUtil;

public abstract class MessageHandler implements IMessageHandler {

	protected int res;

	public MessageHandler() {
	}

	@Override
	public void setResponseCmd(int res) {
		this.res = res;
	}


	public void send(GameError gameError) {
	}

	public Robot getRobot(int accountKey) {
		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);
		Robot robot = robotManager.getRobotByKey(accountKey);
		return robot;
	}

	public <T> T getBean(Class<T> c) {
		return SpringUtil.getBean(c);
	}

}
