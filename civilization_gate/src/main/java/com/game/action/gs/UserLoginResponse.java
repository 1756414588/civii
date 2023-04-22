package com.game.action.gs;

import com.game.action.PacketHandler;
import com.game.domain.UserClient;
import com.game.manager.UserClientManager;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

/**
 * 登录返回消息
 */
public class UserLoginResponse extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
		long channelId = packet.getChannelId();

		UserClient userClient = UserClientManager.getInst().getChannel(channelId);
		int accountKey = userClient.getAccountKey();
		Base msg = getBase(packet);
		if (msg != null) {
			userClient.sendPacket(packet);
		}

//		// 关闭其他在线人
		UserClientManager.getInst().kickClient(accountKey, packet.getChannelId());
	}
}
