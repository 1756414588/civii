package com.game.acion.daily;

import com.game.acion.MessageEvent;
import com.game.cache.UserBuildingCache;
import com.game.constant.BuildingId;
import com.game.domain.Robot;
import com.game.domain.p.DailyMessage;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.BuildingPb.UpBuildingRq;
import com.game.util.BasePbHelper;

/**
 *
 * @Description日常建筑升级
 * @Date 2022/10/21 10:53
 **/

public class UpBuildingDaily extends AutoDaily {

	public UpBuildingDaily(DailyMessage dailyMessage) {
		super(dailyMessage);
	}

	@Override
	public void doAction(MessageEvent event, Robot robot) {
		UserBuildingCache buildingCache = robot.getCache().getBuildingCache();
		if (buildingCache.getWorkQue() != 0) {
			Packet packet = event.createPacket();
			robot.sendPacket(packet);
			return;
		}

		// 司令部等级高于科技馆则升级科技馆
		int buildId = BuildingId.COMMAND;
		if (buildingCache.getCommand() > buildingCache.getTech()) {
			buildId = BuildingId.RESEARCH_COLLEGE;
		}

		UpBuildingRq.Builder upBuildingRq = UpBuildingRq.newBuilder();
		upBuildingRq.setBuildingId(buildId);
		Base.Builder builder = BasePbHelper.createBase(upBuildingRq.build().toByteArray()).toBuilder();
		builder.setParam(event.getEventId());

		Packet packet = PacketCreator.create(builder.build());
		robot.sendPacket(packet);
	}
}
