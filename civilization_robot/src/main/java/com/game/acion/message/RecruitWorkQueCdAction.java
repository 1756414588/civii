package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 秒招募CD
 * @Date 2022/9/15 18:04
 **/

public class RecruitWorkQueCdAction extends MessageAction {

	public RecruitWorkQueCdAction(RobotMessage robotMessage) {
		super(robotMessage);
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
		RobotData robotData = robot.getData();
		robotData.setGuildState(robotData.getGuildState() + 1);
	}
}
