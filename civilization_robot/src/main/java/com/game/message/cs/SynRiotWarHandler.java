package com.game.message.cs;

import com.game.acion.EventAction;
import com.game.acion.events.AttackCountryWarEvent;
import com.game.cache.StaticWorldMapCache;
import com.game.constant.WarType;
import com.game.domain.Robot;
import com.game.manager.MessageManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.WarInfo;
import com.game.pb.WorldPb.SynCountryWarRq;
import com.game.server.TimerServer;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 */
public class SynRiotWarHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynCountryWarRq msg = req.getExtension(SynCountryWarRq.ext);

		MessageManager messageManager = getBean(MessageManager.class);
		WarInfo warInfo = msg.getWarInfo();

		LogHelper.CHANNEL_LOGGER.info("通知 warId:{} accountKey:{}", warInfo.getWarId(), accountKey);

		// 是否已完成今日日常
		Robot robot = getRobot(accountKey);

		// 未完成日常
		if (!messageManager.isComplateDaily(robot.getData())) {
			return;
		}

		// 非国战
		if (warInfo.getWarType() != WarType.ATTACK_COUNTRY) {
			return;
		}

		// 已加入
		if (warInfo.getIsIn()) {
			return;
		}

		int country = robot.getLord().getCountry();
		if (warInfo.getAttackerCountry() != country && warInfo.getDefenceCountry() != country) {
			return;
		}

		StaticWorldMapCache staticWorldMapCache = getBean(StaticWorldMapCache.class);
		int warId = (int) warInfo.getWarId();
		int mapId = staticWorldMapCache.getWorldCity(warInfo.getCityId()).getMapId();

		// 参加国战10s到8分钟之内参加国战
		long delay = 10000 + RandomUtil.getRandomNumber(480000);
		AttackCountryWarEvent event = new AttackCountryWarEvent(robot, new EventAction(), delay, warId, mapId);
		TimerServer.getInst().addDelayEvent(event);

		LogHelper.CHANNEL_LOGGER.info("参加国战信息 warId:{} accountKey:{} delay:{} ", warId, accountKey, delay);
	}
}
