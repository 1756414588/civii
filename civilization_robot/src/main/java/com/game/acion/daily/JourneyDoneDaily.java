package com.game.acion.daily;

import com.game.acion.MessageEvent;
import com.game.cache.UserJourneyCache;
import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.JourneyPb.JourneyDoneRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description通关远征
 * @Date 2022/10/21 10:59
 **/

public class JourneyDoneDaily extends AutoDaily {

	private JourneyDoneRq req;

	public JourneyDoneDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
		this.req = getMsg(JourneyDoneRq.ext);
	}

	@Override
	public void doAction(MessageEvent event, Robot robot) {
		UserJourneyCache journeyCache = robot.getCache().getJourneyCache();
		Packet packet;
		int journeyId = req.getJourneyId();
		if (req.getJourneyId() >= journeyCache.getJourneyId()) {
			packet = event.createPacket();
		} else {
			journeyId = journeyCache.getJourneyId();
			JourneyDoneRq.Builder journeyDoneRq = JourneyDoneRq.newBuilder();
			journeyDoneRq.setJourneyId(journeyCache.getJourneyId());
			journeyDoneRq.addAllHeroId(req.getHeroIdList());
			Base.Builder builder = BasePbHelper.createBase(journeyDoneRq.build().toByteArray()).toBuilder();
			builder.setParam(event.getEventId());
			packet = PacketCreator.create(builder.build());
		}

		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("JourneyDoneDaily journeyId:{}", journeyId);
	}
}
