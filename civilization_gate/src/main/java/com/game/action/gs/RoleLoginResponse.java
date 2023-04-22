package com.game.action.gs;

import com.game.action.PacketHandler;
import com.game.constant.GameError;
import com.game.domain.UserClient;
import com.game.manager.UserClientManager;
import com.game.network.ChannelUtil;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.RoleLoginRs;
import io.netty.channel.ChannelHandlerContext;

/**
 * 登录返回消息
 */
public class RoleLoginResponse extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
		long channelId = packet.getChannelId();
		Base msg = getBase(packet);
		UserClient userClient = UserClientManager.getInst().getChannel(channelId);
		if (msg.getCode() == GameError.OK.getCode()) {
			RoleLoginRs body = msg.getExtension(RoleLoginRs.ext);
			userClient.setRoleId(body.getLordId());
			ChannelUtil.setRoleId(userClient.getCtx(), body.getLordId());
		}
		userClient.sendPacket(packet);
	}

}
