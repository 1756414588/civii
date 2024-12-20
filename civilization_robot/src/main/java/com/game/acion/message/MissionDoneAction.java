package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.MissionDoneRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 通关副本
 * @Date 2022/9/14 19:04
 **/

public class MissionDoneAction extends MessageAction {

	private MissionDoneRq missionDoneRq;

	public MissionDoneAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.missionDoneRq = BasePbHelper.createPb(MissionDoneRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} 关卡ID:{} 出战英雄:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), missionDoneRq.getMissionId(), missionDoneRq.getHeroIdList());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());

		// 体力不足,则等待10分钟之后再继续
		if (base.getCode() == GameError.ENERGY_COST_ERROR.getCode()) {
			tryEvent(messageEvent, 600000L);
			return;
		}

		if (base.getCode() == GameError.OK.getCode()) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}


	}
}
