package com.game.message.cs;

import com.game.cache.UserHeroCache;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.HeroPb.GetHeroRs;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Map;

/**
 * 武将信息
 */
public class GetHeroHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		GetHeroRs msg = req.getExtension(GetHeroRs.ext);

		Robot robot = getRobot(accountKey);
		UserHeroCache userHeroCache = robot.getCache().getHeroCache();

		List<Integer> embattles = userHeroCache.getEmbattles();
		Map<Integer, Integer> heroMap = userHeroCache.getHeroMap();

		embattles.clear();
		// 上阵武将
		msg.getHeroIdList().forEach(e -> {
			if (e <= 0) {
				return;
			}
			if (!embattles.contains(e)) {
				embattles.add(e);
			}
		});

		// 所有武将
		msg.getHeroList().forEach(e -> {
			heroMap.put(e.getHeroId(), e.getHeroId());
		});

	}

}
