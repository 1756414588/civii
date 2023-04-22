package com.game.message.listen;

import com.game.cache.UserHeroCache;
import com.game.domain.BagEquip;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.domain.World;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.EquipPb.WearEquipRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听玩家穿装备
 */
public class ListenWearEquipHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		RobotManager robotManager = getBean(RobotManager.class);

		WearEquipRq msg = req.getExtension(WearEquipRq.ext);
		List<Robot> robotList = robotManager.getRobotList();

		for (Robot robot : robotList) {
			// 获取背包
			UserBagCache userBag = robot.getCache().getBagCache();
			// 获取对应的装备
			BagEquip bagEquip = userBag.getEquipById(msg.getEquipId());
			if (bagEquip == null) {
				LogHelper.CHANNEL_LOGGER.info("装备不存在 robotId:{} equipId:{}", robot.getLord().getLordId(), msg.getEquipId());
				continue;
			}

			WearEquipRq.Builder wearEquipRq = WearEquipRq.newBuilder();

			// 新手引导武将ID为4
			int heroId = msg.getHeroId();
			int equipId = msg.getEquipId();
			// 新手引导穿戴装备
			if (heroId == 4 && (equipId >= 1 && equipId <= 4)) {
				wearEquipRq.setHeroId(heroId);
				wearEquipRq.setKeyId(bagEquip.getKeyId());

				Base base = BasePbHelper.createRqBase(WearEquipRq.EXT_FIELD_NUMBER, WearEquipRq.ext, wearEquipRq.build()).build();
				robot.sendPacket(PacketCreator.create(base));
				continue;
			}

			// 巴蛇
			if (heroId == 5 && (equipId >= 5 && equipId <= 7)) {
				wearEquipRq.setHeroId(heroId);
				wearEquipRq.setKeyId(bagEquip.getKeyId());

				Base base = BasePbHelper.createRqBase(WearEquipRq.EXT_FIELD_NUMBER, WearEquipRq.ext, wearEquipRq.build()).build();
				robot.sendPacket(PacketCreator.create(base));
				continue;
			}

			UserHeroCache userHeroCache = robot.getCache().getHeroCache();
			heroId = userHeroCache.getEmptyEmbattle();//获取上阵武将
			if (heroId < 1) {
				LogHelper.CHANNEL_LOGGER.info("没有武将上阵 robotId:{}", robot.getLord().getLordId());
				continue;
			}

			// 向服务器请求穿戴装备
			WearEquipRq.Builder builder = WearEquipRq.newBuilder();
			builder.setHeroId(heroId);
			builder.setKeyId(bagEquip.getKeyId());

			Base base = BasePbHelper.createRqBase(WearEquipRq.EXT_FIELD_NUMBER, WearEquipRq.ext, builder.build()).build();
			robot.sendPacket(PacketCreator.create(base));
		}

	}
}
