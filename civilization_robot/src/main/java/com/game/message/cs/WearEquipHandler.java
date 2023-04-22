package com.game.message.cs;

import com.game.domain.BagEquip;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Equip;
import com.game.pb.EquipPb.WearEquipRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

/**
 * 穿戴装备
 */
public class WearEquipHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		WearEquipRs msg = req.getExtension(WearEquipRs.ext);

		Robot robot = getRobot(accountKey);
		UserBagCache userBag = robot.getCache().getBagCache();

		Map<Integer, BagEquip> bagEquipMap = userBag.getBagEquipMap();

		// 有脱下来的装备
		if (msg.hasAddEquipItem()) {
			Equip equip = msg.getAddEquipItem();
			bagEquipMap.put(equip.getKeyId(), new BagEquip(equip));
		}

		// 扣除的装备
		if (msg.hasRemoveEquipItemId()) {
			bagEquipMap.remove(msg.getRemoveEquipItemId());
		}
	}
}
