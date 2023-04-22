package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.PacketCreator;
import com.game.pb.RolePb.RoleLoginRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;

/**
 *
 * @Description 角色登录
 * @Date 2022/9/19 10:50
 **/

@Component
public class RoleLoginAction extends EnterGameAction {

	public RoleLoginAction() {
		RoleLoginRq.Builder builder = RoleLoginRq.newBuilder();
		builder.setReconnect(false);

		requestPacket = PacketCreator.create(BasePbHelper.createRqBase(RoleLoginRq.EXT_FIELD_NUMBER, RoleLoginRq.ext, builder.build()).build());
	}

	@Override
	public void doAction(MessageEvent messageEvent,Robot robot) {
		long eventId = messageEvent.getEventId();
		robot.sendPacket(requestPacket);
		LogHelper.CHANNEL_LOGGER.info("[游戏.角色登录] accountKey:{} eventId:{} cmd:{}", robot.getId(), eventId, requestPacket.getCmd());
	}
}
