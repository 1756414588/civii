package com.game.message.listen;

import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.server.RobotServer;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

public class ListenEventHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		LogHelper.CHANNEL_LOGGER.info("ListenEventHandler success....");

		// 机器人服务器准备完毕
		RobotServer.getInst().setReady(true);
	}
}
