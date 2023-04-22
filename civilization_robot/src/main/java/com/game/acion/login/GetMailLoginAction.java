package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.MailPb.GetMailRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/16 17:42
 **/

public class GetMailLoginAction extends EnterGameAction {

	public GetMailLoginAction(int respondCode, Packet packet) {
		super(respondCode, packet);
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		GetMailRq.Builder builder = GetMailRq.newBuilder();
		builder.setPage(1).setType(1).build();
		Base.Builder base = BasePbHelper.createRqBase(requestCode, eventId, GetMailRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));

		LogHelper.CHANNEL_LOGGER.info("[登录.消息] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, requestPacket.getCmd());
	}
}
