package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.TechKillCdByGoldEvent;
import com.game.acion.events.impl.TechKillCdByGoldAction;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.server.TimerServer;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 完成科技
 * @Date 2022/9/15 18:04
 **/

public class TechLevelupAction extends MessageAction {

	public TechLevelupAction(RobotMessage robotMessage) {
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
		if (base.getCode() == 200 || base.getCode() == GameError.NO_TECH_IS_LEVEL_UPING.getCode()) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}

		// 科技未完成,则使用砖石去秒
		long delayTime = 100L;
		TechKillCdByGoldEvent killCdByGoldEvent = new TechKillCdByGoldEvent(robot, new TechKillCdByGoldAction(), delayTime);
		TimerServer.getInst().addDelayEvent(killCdByGoldEvent);

		tryEvent(messageEvent, 5000);
	}

}
