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
import com.game.pb.ActivityPb.DoActTaskHeroRewardRq;
import com.game.pb.BasePb.Base;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 活动奖励领取 活动111无畏尖兵
 * @Date 2022/9/15 18:04
 **/

public class DoActTaskHeroRewardAction extends MessageAction {

	private DoActTaskHeroRewardRq doActTaskHeroRewardRq;

	public DoActTaskHeroRewardAction(RobotMessage robotMessage) {
		super(robotMessage);
		doActTaskHeroRewardRq = BasePbHelper.createPb(DoActTaskHeroRewardRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} taskId:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), doActTaskHeroRewardRq.getTaskId());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());

		// 已完成则直接通过
		if (base.getCode() == GameError.OK.getCode() || base.getCode() == GameError.ACTIVITY_NOT_OPEN.getCode()) {
			Record record = robot.getRecord();
			record.setState(record.getState() + 1);
			return;
		}
	}
}
