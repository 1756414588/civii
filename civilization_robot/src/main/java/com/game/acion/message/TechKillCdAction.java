package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddGoldEvent;
import com.game.acion.events.TechKillCdByGoldEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 秒科研进度
 * @Date 2022/9/15 18:04
 **/

public class TechKillCdAction extends MessageAction {

	public TechKillCdAction(RobotMessage robotMessage) {
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

		// 如果砖石不足,则使用添加砖石
		if (base.getCode() == GameError.NOT_ENOUGH_GOLD.getCode()) {
			AddGoldEvent addGoldEvent = new AddGoldEvent(robot, new EventAction(), 500, 10L);
			tryEvent(addGoldEvent);

			// 添加砖石后继续秒CD
			tryEvent(messageEvent, 2000L);
			return;
		}

		// 如果是免费次数没有,则使用砖石秒CD
		TechKillCdByGoldEvent techKillCdByGoldEvent = new TechKillCdByGoldEvent(robot, this, 100);
		tryEvent(techKillCdByGoldEvent);
	}

}
