package com.game.network.robot;


import com.game.domain.Robot;
import com.game.network.listen.ListenNet;
import com.game.server.AppPropertes;

import com.game.spring.SpringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 机器人连接
 */
@Getter
@Setter
public class RobotNet extends ListenNet {

	private Robot robot;

	public RobotNet(Robot robot) {
		this.robot = robot;
	}

	/**
	 * 机器人的处理器
	 */
	public void initRobot() {
		AppPropertes property = SpringUtil.getBean(AppPropertes.class);
		this.packetHandler = new RobotPacketHandler();
		this.context = new RobotNetContext(robot, property.getGateIp(), property.getGatePort(), packetHandler);
	}


}
