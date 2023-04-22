package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.constant.GameError;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.UpdateGuideRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 新手引导
 * @Date 2022/9/14 19:04
 **/

public class UpdateGuideAction extends MessageAction {

	private UpdateGuideRq updateGuideRq;

	public UpdateGuideAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.updateGuideRq = BasePbHelper.createPb(UpdateGuideRq.ext, robotMessage.getContent());
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
		if (base.getCode() == GameError.OK.getCode()) {
			robot.setGuideKey(updateGuideRq.getGuideKey());
			Record record = robot.getRecord();
			record.setState(record.getState() + 1);
		}
	}
}
