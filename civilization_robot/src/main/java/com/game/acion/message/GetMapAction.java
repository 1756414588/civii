package com.game.acion.message;

import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.WorldPos;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.GetMapRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 请求地图上的信息
 * @Date 2022/9/16 17:42
 **/

public class GetMapAction extends MessageAction {

	public GetMapAction(RobotMessage robotMessage) {
		super(robotMessage);
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		int robotX = robot.getLord().getPosX();
		int robotY = robot.getLord().getPosY();
		GetMapRq.Builder builder = GetMapRq.newBuilder();
		builder.setPos(Pos.newBuilder().setX(robotX).setY(robotY).build());
		Base.Builder base = BasePbHelper.createRqBase(requestCode, eventId, GetMapRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} pos:{}", robot.getId(), requestCode, eventId, id, getName(), new WorldPos(robotX, robotY));
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());
		if (base.getCode() == 200) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
		}
	}

}
