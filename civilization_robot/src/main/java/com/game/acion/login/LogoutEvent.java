package com.game.acion.login;

import com.game.domain.Robot;
import com.game.manager.LoginManager;
import com.game.spring.SpringUtil;
import com.game.timer.TimerEvent;

/**
 * @Author 陈奎
 * @Description退出事件
 * @Date 2022/10/26 10:26
 **/

public class LogoutEvent extends TimerEvent {

	private Robot robot;

	public LogoutEvent(Robot robot, long delayTime) {
		super(1, delayTime);
		this.robot = robot;
	}

	@Override
	public void action() {
		LoginManager loginManager = SpringUtil.getBean(LoginManager.class);
		loginManager.logout(robot);
	}
}
