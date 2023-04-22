package com.game.message.cs;

import com.game.cache.MapMonsterCache;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.CommonPb.WorldEntity;
import com.game.pb.WorldPb.SynEntityRq;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 世界上的实体消失
 */
public class SynEntityHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynEntityRq msg = req.getExtension(SynEntityRq.ext);

		Robot robot = getRobot(accountKey);

		// 通知实体消失
		WorldEntity worldEntity = msg.getEntity();
		Pos pos = msg.getOldPos();
		int maxMonsterLv = msg.getMaxMonsterLv();

		if (worldEntity.getEntityType() == 1) {// 野怪消失
			World world = robot.getWorld();
			world.setMaxMonsterLv(maxMonsterLv);
		}

		getBean(MapMonsterCache.class).synEntityRq(worldEntity, pos);
	}


}
