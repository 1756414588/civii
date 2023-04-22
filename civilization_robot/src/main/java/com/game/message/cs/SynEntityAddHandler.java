package com.game.message.cs;

import com.game.cache.UserMapCache;
import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.SynEntityAddRq;
import io.netty.channel.ChannelHandlerContext;

/**
 * 添加实体
 */
public class SynEntityAddHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynEntityAddRq msg = req.getExtension(SynEntityAddRq.ext);

		// 野怪缓存
		Robot robot = getRobot(accountKey);
		UserMapCache userMapCache = robot.getCache().getMapCache();

		msg.getEntityList().forEach(e -> {
			userMapCache.add(e);
		});
	}

}
