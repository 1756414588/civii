package com.game.message.cs;

import com.game.cache.UserHeroCache;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.HeroPb.EmbattleHeroRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Map;

/**
 * 上阵武将信息
 */
public class EmbattleHeroHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		EmbattleHeroRs msg = req.getExtension(EmbattleHeroRs.ext);

		Robot robot = getRobot(accountKey);
		UserHeroCache userHeroCache = robot.getCache().getHeroCache();

		List<Integer> embattles = userHeroCache.getEmbattles();

		embattles.clear();
		msg.getEmbattleHeroList().forEach(e -> {
			if (e <= 0) {
				return;
			}
			embattles.add(e);
		});
	}

}