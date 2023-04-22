package com.game.acion;

import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.domain.p.RobotData;
import com.game.manager.MessageEventManager;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;

/**
 *
 * @Description 日常行为
 * @Date 2022/10/21 10:18
 **/

public abstract class DailyAction implements IAction {

	// 唯一标志
	protected long id;
	// 请求消息编码
	protected int requestCode;
	// 应答消息编码
	protected int respondCode;
	// 消息
	protected DailyMessage dailyMessage;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void registerEvent(Robot robot) {
		RobotData robotData = robot.getData();
		robotData.setMessageId(dailyMessage.getKeyId());
		MessageEvent messageEvent = new MessageEvent(robot, this, this.getRemain());
		SpringUtil.getBean(MessageEventManager.class).registerEvent(robot, messageEvent.getEventId(), messageEvent);
		TimerServer.getInst().addDelayEvent(messageEvent);
	}

	@Override
	public boolean isCompalte(Robot robot) {
		if (dailyMessage != null) {
			RobotData robotDaily = robot.getData();
			return robotDaily.getStatus() == 2;
		}
		return true;
	}

	@Override
	public long getRemain() {
		if (dailyMessage != null) {
			return dailyMessage.getRemainTime();
		}
		return 100;
	}

	@Override
	public int getGroup() {
		return 0;
	}


	@Override
	public byte[] getMessage() {
		return dailyMessage.getContent();
	}
}
