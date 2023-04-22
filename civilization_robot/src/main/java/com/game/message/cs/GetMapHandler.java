package com.game.message.cs;

import com.game.cache.MapMonsterCache;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.GetMapRs;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 获取坐标周围的
 */
public class GetMapHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
//		GetMapRs msg = req.getExtension(GetMapRs.ext);
//
//		// 野怪缓存
//		MapMonsterCache mapMonsterCache = getBean(MapMonsterCache.class);
//		msg.getAddEntitiesList().forEach(e -> {
//
//			mapMonsterCache.entityAdd(e);
//		});


	}

}
