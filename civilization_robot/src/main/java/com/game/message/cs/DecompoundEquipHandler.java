package com.game.message.cs;

import com.game.domain.BagEquip;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.EquipPb.DecompoundEquipRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

/**
 * 分解装备
 */
public class DecompoundEquipHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		DecompoundEquipRs msg = req.getExtension(DecompoundEquipRs.ext);

		Robot robot = getRobot(accountKey);
		UserBagCache userBag = robot.getCache().getBagCache();

		Map<Integer, BagEquip> bagEquipMap = userBag.getBagEquipMap();

		if (msg.getKeyIdCount() > 0) {
			msg.getKeyIdList().forEach(e -> {
				bagEquipMap.remove(e.intValue());
			});
		}
	}

}
