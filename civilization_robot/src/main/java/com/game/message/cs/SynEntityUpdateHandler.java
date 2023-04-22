package com.game.message.cs;

import com.game.cache.MapMonsterCache;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.WorldEntity;
import com.game.pb.WorldPb.SynEntityUpdateRq;
import io.netty.channel.ChannelHandlerContext;

/**
 * 获取坐标周围的
 */
public class SynEntityUpdateHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynEntityUpdateRq msg = req.getExtension(SynEntityUpdateRq.ext);

		WorldEntity worldEntity = msg.getEntity();
		getBean(MapMonsterCache.class).synEntityUpdateRq(worldEntity);
	}

}
