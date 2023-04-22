package com.game.message.cs;

import com.game.cache.ConfigCache;
import com.game.cache.StaticWorldMapCache;
import com.game.cache.WorldCacheManager;
import com.game.constant.WarType;
import com.game.domain.WarCache;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.WarInfo;
import com.game.pb.WorldPb.SynCountryWarRq;
import com.game.spring.SpringUtil;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import com.game.util.TimeHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 通知国战信息
 */
public class SynCountryWarHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynCountryWarRq msg = req.getExtension(SynCountryWarRq.ext);

		WarInfo warInfo = msg.getWarInfo();

		// 非国战
		if (warInfo.getWarType() != WarType.ATTACK_COUNTRY) {
			return;
		}

		ConfigCache configCache = SpringUtil.getBean(ConfigCache.class);
		int ran = RandomUtil.getRandomNumber(100);
		int attendCountry = configCache.getIntValue("attend_country");
		if (ran > attendCountry) {
			return;
		}

		StaticWorldMapCache staticWorldMapCache = getBean(StaticWorldMapCache.class);
		WorldCacheManager worldCacheManager = getBean(WorldCacheManager.class);

		int mapId = staticWorldMapCache.getWorldCity(warInfo.getCityId()).getMapId();

		WarCache warCache = new WarCache(warInfo);
		warCache.setMapId(mapId);
		WarCache entity = worldCacheManager.putWar(warCache);
		if (entity == null) {
			warCache.initAttackTime();
		}

		LogHelper.CHANNEL_LOGGER.info("国战通知 warId:{} accountKey:{} country:{}", warInfo.getWarId(), accountKey, warInfo.getAttackerCountry());
	}
}
