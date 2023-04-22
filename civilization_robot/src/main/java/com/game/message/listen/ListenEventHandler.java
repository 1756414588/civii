package com.game.message.listen;

import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

public class ListenEventHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		LogHelper.CHANNEL_LOGGER.info("ListenEventHandler success....");

		// 加载机器人
		RobotManager robotManager = getBean(RobotManager.class);
		robotManager.createRobot();
	}
}
