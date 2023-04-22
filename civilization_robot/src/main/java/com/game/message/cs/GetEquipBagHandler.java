package com.game.message.cs;

import com.game.domain.BagEquip;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.EquipPb.GetEquipBagRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

/**
 * 获取背包信息
 */
public class GetEquipBagHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		GetEquipBagRs msg = req.getExtension(GetEquipBagRs.ext);

		Robot robot = getRobot(accountKey);

		UserBagCache userBag = robot.getCache().getBagCache();
		Map<Integer, BagEquip> bagEquipMap = userBag.getBagEquipMap();

		msg.getEquipItemList().forEach(e -> {
			bagEquipMap.put(e.getKeyId(), new BagEquip(e));
		});

	}

}
