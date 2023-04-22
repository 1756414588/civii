package com.game.acion;

import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.manager.MessageEventManager;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;

/**
 * @Author 陈奎
 * @Description消息行为
 * @Date 2022/9/9 17:50
 **/

public abstract class MessageAction implements IAction {

	// 唯一标志
	protected long id;
	// 请求消息编码
	protected int requestCode;
	// 应答消息编码
	protected int respondCode;
	// 消息
	protected RobotMessage robotMessage;

	public MessageAction(RobotMessage robotMessage) {
		this.id = robotMessage.getKeyId();
		this.requestCode = robotMessage.getRequestCode();
		this.respondCode = robotMessage.getRespondCode();
		this.robotMessage = robotMessage;
	}


	@Override
	public long getId() {
		return id;
	}

	public <T> T getBean(Class<T> requiredType) {
		return SpringUtil.getBean(requiredType);
	}

	@Override
	public boolean isCompalte(Robot robot) {
		if (robot.getGuildId() != id) {
			return false;
		}
		return robot.getGuildState() > 0;
	}

	@Override
	public void registerEvent(Robot robot) {
		MessageEvent messageEvent = new MessageEvent(robot, this, this.getRemain());
		SpringUtil.getBean(MessageEventManager.class).registerEvent(robot, messageEvent.getEventId(), messageEvent);

		// 记录
		RobotData data = robot.getData();
		data.setGuildId(id);
		data.setGuildState(0);
		TimerServer.getInst().addDelayEvent(messageEvent);
	}

	public void tryEvent(MessageEvent messageEvent) {
		SpringUtil.getBean(MessageEventManager.class).registerEvent(messageEvent);
		TimerServer.getInst().addDelayEvent(messageEvent);
	}

	public void tryEvent(MessageEvent messageEvent, long delay) {
		messageEvent.reset(delay);
		SpringUtil.getBean(MessageEventManager.class).registerEvent(messageEvent);
		TimerServer.getInst().addDelayEvent(messageEvent);
	}

	@Override
	public long getRemain() {
		return robotMessage.getRemainTime();
	}

	public int getType() {
		return robotMessage.getType();
	}

	public int getParentId() {
		return robotMessage.getParentId();
	}

	public String getName() {
		return robotMessage.getName() == null ? "" : robotMessage.getName();
	}

	@Override
	public byte[] getMessage() {
		return robotMessage.getContent();
	}

	@Override
	public int getGroup() {
		return robotMessage.getDiffHour();
	}
}
