package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddExpEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.GetLevelAwardRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 领取升级奖励
 * @Date 2022/9/14 19:04
 **/

public class GetLevelAwardAction extends MessageAction {

	private GetLevelAwardRq getLevelAwardRq;

	public GetLevelAwardAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.getLevelAwardRq = BasePbHelper.createPb(GetLevelAwardRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} level:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), getLevelAwardRq.getLevel());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());
		// 领取失败,则等待五秒再次领取
		if (base.getCode() == GameError.OK.getCode()) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}

		// 等级奖励不存在,则给玩家升级
		if (base.getCode() == GameError.NO_LEVEL_AWARD.getCode()) {
			AddExpEvent addExpEvent = new AddExpEvent(robot, new EventAction(), getLevelAwardRq.getLevel(), 100);
			tryEvent(addExpEvent);

			// 领取
			tryEvent(messageEvent, 2000);
		}
	}

}
