package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddGoldEvent;
import com.game.constant.GameError;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;

/**
 *
 * @Description 秒建筑CD
 * @Date 2022/9/15 18:04
 **/

public class BuyBuildQueCdAction extends MessageAction {

	public BuyBuildQueCdAction(RobotMessage robotMessage) {
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

		// 已完成则直接通过
		if (base.getCode() == GameError.OK.getCode() || base.getCode() == GameError.BUILDQUE_NOT_EXISTS.getCode()) {
			Record record = robot.getRecord();
			record.setState(record.getState() + 1);
			return;
		}

		// 砖石不足,则添加砖石继续秒
		if (base.getCode() == GameError.NOT_ENOUGH_GOLD.getCode()) {
			AddGoldEvent addGoldEvent = new AddGoldEvent(robot, new EventAction(), 1000, 100L);
			tryEvent(addGoldEvent);

			// 继续秒CD
			tryEvent(messageEvent, 2000);
			return;
		}

	}
}
