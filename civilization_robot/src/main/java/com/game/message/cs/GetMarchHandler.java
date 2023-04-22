package com.game.message.cs;

import com.game.cache.UserHeroCache;
import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.GetMarchRs;
import io.netty.channel.ChannelHandlerContext;

/**
 * 获取行军信息
 */
public class GetMarchHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		GetMarchRs msg = req.getExtension(GetMarchRs.ext);
		Robot robot = getRobot(accountKey);

		// 武将缓存数据
		UserHeroCache heroCache = robot.getCache().getHeroCache();
		heroCache.getArmys().clear();

		msg.getMarchList().forEach(e -> {
			if (e.getLordId() == robot.getLord().getLordId()) {
				e.getHeroIdList().forEach(heroId -> {
					heroCache.getArmys().put(heroId, heroId);
				});
			}
		});
	}

}
