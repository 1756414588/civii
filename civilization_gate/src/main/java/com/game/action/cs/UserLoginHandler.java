package com.game.action.cs;

import com.game.action.PacketHandler;
import com.game.domain.UserClient;
import com.game.manager.UserClientManager;
import com.game.packet.Packet;
import com.game.pb.RolePb.UserLoginRq;
import com.game.server.GateServer;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 登录请求消息消息
 */
public class UserLoginHandler extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
		long channelId = packet.getChannelId();

		UserLoginRq userLoginRq = getMsg(packet, UserLoginRq.ext);

		UserClient userClient = UserClientManager.getInst().getChannel(channelId);
		userClient.setAccountKey(userLoginRq.getKeyId());

		GateServer.getInst().getNet().send(packet);
		LogHelper.CHANNEL_LOGGER.info("channelId:{} UserLoginRq:{}", channelId, userLoginRq);
	}

}
