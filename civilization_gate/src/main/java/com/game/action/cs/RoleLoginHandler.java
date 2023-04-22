package com.game.action.cs;

import com.game.action.PacketHandler;
import com.game.packet.Packet;
import com.game.server.GateServer;
import io.netty.channel.ChannelHandlerContext;

/**
 * 登录请求消息消息
 */
public class RoleLoginHandler extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
//		long channelId = ChannelUtil.getChannelId(ctx);
//		Base msg = req.toBuilder().setParam(channelId).build();
//		Packet packet = PacketCreator.create(msg.getCommand(), msg.toByteArray(), roleId, channelId);
		GateServer.getInst().getNet().send(packet);
//		LogHelper.CHANNEL_LOGGER.info("RoleLoginRq: {}", msg);
	}

}
