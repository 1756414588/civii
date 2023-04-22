package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.SoldierPb.RecruitSoldierRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description训练士兵
 * @Date 2022/9/27 15:15
 **/

public class RecruitSoldierAction extends MessageAction {

	RecruitSoldierRq recruitSoldierRq;

	public RecruitSoldierAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.recruitSoldierRq = BasePbHelper.createPb(RecruitSoldierRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} 兵营:{} ", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), recruitSoldierRq.getSoldierType());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		RobotData robotData = robot.getData();
		robotData.setGuildState(robotData.getGuildState() + 1);
	}
}
