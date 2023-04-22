package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.constant.GameError;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.TaskPb.TaskAwardRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 领取任务奖励
 * @Date 2022/9/14 19:04
 **/

public class TaskAwardAction extends MessageAction {

	private TaskAwardRq taskAwardRq;

	public TaskAwardAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.taskAwardRq = BasePbHelper.createPb(TaskAwardRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());

		// 领取失败,则等待五秒再次领取
		if (base.getCode() != GameError.OK.getCode()) {
			tryEvent(messageEvent, 5000);

			LogHelper.CHANNEL_LOGGER.info("msg:{}", taskAwardRq);
			return;
		}

		Record record = robot.getRecord();
		record.setState(record.getState() + 1);
	}

}
