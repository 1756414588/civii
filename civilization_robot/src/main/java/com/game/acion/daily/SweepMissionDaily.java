package com.game.acion.daily;


import com.game.acion.MessageEvent;
import com.game.cache.UserMissionCache;
import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.MissionDoneRq;
import com.game.pb.MissionPb.SweepMissionRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description扫荡关卡
 * @Date 2022/10/21 15:35
 **/

public class SweepMissionDaily extends AutoDaily {

	SweepMissionRq req;

	public SweepMissionDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
		this.req = getMsg(SweepMissionRq.ext);
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
			SweepMissionRq.Builder sweepMissionRq = SweepMissionRq.newBuilder();
			sweepMissionRq.setMissionId(missionCache.getStarThreeId());

			Base.Builder builder = BasePbHelper.createBase(sweepMissionRq.build().toByteArray()).toBuilder();
			builder.setParam(event.getEventId());
			packet = PacketCreator.create(builder.build());
		}

		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("SweepMissionDaily missionId:{}", missionId);
	}


}
