package com.game.message.cs;

import com.game.cache.MapMonsterCache;
import com.game.domain.WorldPos;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb;
import com.game.pb.MapInfoPb.GetMapNpcRs;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

/**
 * 攻打野怪后，立马行军加速
 */
public class GetMapNpcHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
//		LogHelper.CHANNEL_LOGGER.info("同步怪物信息返回");
		GetMapNpcRs msg = req.getExtension(GetMapNpcRs.ext);
		MapMonsterCache mapMonsterCache = getBean(MapMonsterCache.class);

		Map<WorldPos, WorldPos> map = mapMonsterCache.getMapPos(msg.getMapId());

		msg.getNpcListList().forEach(value -> {

			int level = (int) (value / 1000000);
			int x = (int) (value % 1000000) / 1000;
			int y = (int) (value % 1000);
			WorldPos worldPos = new WorldPos(x, y, level);
			if (map.containsKey(worldPos)) {
				return;
			}
			map.put(worldPos, worldPos);
		});
	}
}
