package com.game.timer;

import com.game.define.AppTimer;
import com.game.domain.p.RobotData;
import com.game.manager.LoginManager;
import com.game.manager.RobotManager;
import com.game.server.RobotServer;
import com.game.spring.SpringUtil;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/10/20 14:30
 **/

@AppTimer(desc = "登录")
public class LoginTimer extends TimerEvent {

	private long loginTime;

	public LoginTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		if (!RobotServer.getInst().isReady()) {// 未准备
			return;
		}

		LoginManager loginManager = SpringUtil.getBean(LoginManager.class);

		//
		if (loginManager.isFullOnline()) {
			return;
		}

		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);

		long curTime = System.currentTimeMillis();
		if (curTime < (loginTime + loginManager.getLoginRate())) {
			return;
		}

		// 记录登录时间
		loginTime = curTime;

		RobotData robotData = loginManager.getNextLoginRobot();
		if (robotData == null) {
			return;
		}

		// 同一个账号登录冷却3分钟,避免重新选择同一个账号进行登录
		robotData.setLoginCDTime(curTime + 180000);

		// 创建机器人,并且连接服务器进行登录
		robotManager.createRobotThenLogin(robotData);
	}
}
