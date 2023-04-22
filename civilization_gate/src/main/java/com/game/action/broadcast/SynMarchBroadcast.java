package com.game.action.broadcast;

import com.game.action.PacketHandler;
import com.game.manager.UserClientManager;
import com.game.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * 登录返回消息
 */
public class SynMarchBroadcast extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
		UserClientManager.getInst().getChannels().values().forEach(e -> {
			if (e.getRoleId() > 0) {//已登录用户
				e.sendPacket(packet);
			}
		});
	}

}
