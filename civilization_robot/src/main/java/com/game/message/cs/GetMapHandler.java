package com.game.message.cs;

import com.game.cache.UserMapCache;
import com.game.domain.Robot;
import com.game.domain.WorldPos;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.GetMapRs;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取坐标周围的
 */
public class GetMapHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		GetMapRs msg = req.getExtension(GetMapRs.ext);

		// 野怪缓存
		Robot robot = getRobot(accountKey);
		UserMapCache userMapCache = robot.getCache().getMapCache();

		List<WorldPos> list = new ArrayList<>();
		msg.getAddEntitiesList().forEach(e -> {
			if (e.getEntityType() == 1) {
				WorldPos worldPos = new WorldPos(e.getPos().getX(), e.getPos().getY(), e.getLevel());
				list.add(worldPos);
			}
			userMapCache.add(e);
		});

	}

}
