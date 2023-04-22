package com.game.message.cs;

import com.game.cache.UserMapCache;
import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.WorldEntity;
import com.game.pb.WorldPb.SynEntityRq;
import io.netty.channel.ChannelHandlerContext;

/**
 * 世界上的实体消失
 */
public class SynEntityHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynEntityRq msg = req.getExtension(SynEntityRq.ext);

		// 野怪缓存
		Robot robot = getRobot(accountKey);
		UserMapCache userMapCache = robot.getCache().getMapCache();

		// 通知实体消失
		msg.getOldPos();

		if (msg.hasMaxMonsterLv()) {
			int maxLevel = userMapCache.getMaxLevel() > msg.getMaxMonsterLv() ? userMapCache.getMaxLevel() : msg.getMaxMonsterLv();
			userMapCache.setMaxLevel(maxLevel);
		}

		if (msg.hasEntity()) {//消失的实体
			userMapCache.remove(msg.getEntity());
		}

		if (msg.hasOldPos()) {//消失的坐标
			userMapCache.remove(msg.getOldPos().getX(), msg.getOldPos().getY());
		}
	}


}
