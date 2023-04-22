package com.game.acion.daily;

import com.game.acion.MessageEvent;
import com.game.cache.UserMissionCache;
import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.MissionDoneRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description通关副本
 * @Date 2022/10/21 10:59
 **/

public class MissionDoneDaily extends AutoDaily {

	private MissionDoneRq req;

	public MissionDoneDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
		this.req = getMsg(MissionDoneRq.ext);
	}

	@Override
	public void doAction(MessageEvent event, Robot robot) {
		UserMissionCache missionCache = robot.getCache().getMissionCache();
		Packet packet;
		int missionId = req.getMissionId();
		if (missionId >= missionCache.getMaxId()) {
			packet = event.createPacket();
		} else {
			missionId = missionCache.getMaxId();
			MissionDoneRq.Builder missionDoneRq = MissionDoneRq.newBuilder();
			missionDoneRq.addAllHeroId(req.getHeroIdList());
			missionDoneRq.setMissionId(missionId);
			Base.Builder builder = BasePbHelper.createBase(missionDoneRq.build().toByteArray()).toBuilder();
			builder.setParam(event.getEventId());
			packet = PacketCreator.create(builder.build());
		}

		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("MissionDoneDaily missionId:{}", missionId);
	}

}
