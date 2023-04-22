package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.GetMapRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description
 * @Date 2022/9/16 17:42
 **/

public class GetMapLoginAction extends EnterGameAction {

	public GetMapLoginAction(int respondCode, Packet packet) {
		super(respondCode, packet);
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		int robotX = robot.getLord().getPosX();
		int robotY = robot.getLord().getPosY();
		GetMapRq.Builder builder = GetMapRq.newBuilder();
		builder.setPos(Pos.newBuilder().setX(robotX).setY(robotY).build());
		Base.Builder base = BasePbHelper.createRqBase(GetMapRq.EXT_FIELD_NUMBER, eventId, GetMapRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));

		LogHelper.CHANNEL_LOGGER.info("[登录.消息] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, requestPacket.getCmd());
	}
}
