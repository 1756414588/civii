package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.pb.BasePb.Base;
import com.game.pb.OmamentPb.WearOmamentRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description
 * @Date 2022/9/14 19:04
 **/

public class WearOmamentAction extends MessageAction {

	private WearOmamentRq wearOmamentRq;

	public WearOmamentAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.wearOmamentRq = BasePbHelper.createPb(WearOmamentRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		robot.sendPacket(messageEvent.createPacket());
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} pos:{} omamentId:{}", robot.getId(), requestCode, eventId, id, getName(), wearOmamentRq.getPos(), wearOmamentRq.getOmamentId());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());
		if (base.getCode() == GameError.OK.getCode()) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}

		// 装备部不存在
		if (base.getCode() == GameError.NOT_ENOUGH_OMAMENT.getCode()) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}
	}
}
