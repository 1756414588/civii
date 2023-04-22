package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddSoldierNumberEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.AddSoldierRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 英雄补兵
 * @Date 2022/9/15 18:04
 **/

public class AddSoldierAction extends MessageAction {

	private AddSoldierRq addSoldierRq;

	public AddSoldierAction(RobotMessage robotMessage) {
		super(robotMessage);
		addSoldierRq = BasePbHelper.createPb(AddSoldierRq.ext, robotMessage.getContent());
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

		// 已完成则直接通过
		if (base.getCode() == GameError.OK.getCode()) {
			RobotData record = robot.getData();
			record.setGuildState(record.getGuildState() + 1);
			return;
		}

		// 带兵量不足,补充兵力
		if (base.getCode() == GameError.NO_SOLDIER_COUNT.getCode()) {
			AddSoldierNumberEvent addSoldierNumberEvent = new AddSoldierNumberEvent(robot, new EventAction(), addSoldierRq.getHeroId(), 100);
			tryEvent(addSoldierNumberEvent);

			// 补充兵量后,继续给武将补充兵力
			tryEvent(messageEvent, 2000);
			return;
		}
	}
}
