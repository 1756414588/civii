package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.BuildingPb.RecoverBuildRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 收复建筑
 * @Date 2022/9/15 18:04
 **/

public class RecoverBuildAction extends MessageAction {

	RecoverBuildRq recoverBuildRq;

	public RecoverBuildAction(RobotMessage robotMessage) {
		super(robotMessage);
		recoverBuildRq = BasePbHelper.createPb(RecoverBuildRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} 建筑ID:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), recoverBuildRq.getBuild());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());
		if (base.getCode() == 200 || base.getCode() == 10705) {//建筑已收复也可以通过
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
		}
	}
}
