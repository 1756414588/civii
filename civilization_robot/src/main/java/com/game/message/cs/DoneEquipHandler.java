package com.game.message.cs;

import com.game.domain.BagEquip;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Equip;
import com.game.pb.EquipPb.DoneEquipRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

/**
 * 装备打造完成响应
 */
public class DoneEquipHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		DoneEquipRs msg = req.getExtension(DoneEquipRs.ext);

		Robot robot = getRobot(accountKey);
		UserBagCache userBag = robot.getCache().getBagCache();

		Map<Integer, BagEquip> bagEquipMap = userBag.getBagEquipMap();
		Equip equip = msg.getEquipItem();

		bagEquipMap.put(equip.getKeyId(), new BagEquip(msg.getEquipItem()));
	}

}
