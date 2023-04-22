package com.game.message.cs;

import com.game.cache.MapMonsterCache;
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

		MapMonsterCache mapMonsterCache = getBean(MapMonsterCache.class);
		msg.getEntityList().forEach(e -> {
			mapMonsterCache.entityAdd(e);
		});

	}

}
