package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.AddGoldEvent;
import com.game.acion.events.RecruitWorkQueCdByGoldEvent;
import com.game.acion.events.impl.RecruitWorkQueCdByGoldAction;
import com.game.constant.BuildingId;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.pb.BuildingPb.BuyBuildQueCdRs;
import com.game.pb.BuildingPb.UpBuildingRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 升级建筑
 * @Date 2022/9/15 18:04
 **/

public class UpBuildingAction extends MessageAction {

	UpBuildingRq buildingRq;

	public UpBuildingAction(RobotMessage robotMessage) {
		super(robotMessage);
		buildingRq = BasePbHelper.createPb(UpBuildingRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		Packet packet = messageEvent.createPacket();
		robot.sendPacket(packet);
		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} buildingId:{}", robot.getId(), requestCode, messageEvent.getEventId(), id, getName(), buildingRq.getBuildingId());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());


		// 已完成则直接通过
		if (base.getCode() == GameError.OK.getCode() || base.getCode() == GameError.BUILDQUE_NOT_EXISTS.getCode()) {
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}

		// 砖石不足,则添加砖石继续秒
		if (base.getCode() == GameError.NOT_ENOUGH_GOLD.getCode()) {
			AddGoldEvent addGoldEvent = new AddGoldEvent(robot, new EventAction(), 1000, 100L);
			tryEvent(addGoldEvent);

			// 继续秒CD
			tryEvent(messageEvent, 2000);
			return;
		}

		// 升级的时候士兵正在训练,秒训练CD
		if (base.getCode() == GameError.SOLDIER_IS_TRAINING.getCode()) {
			// 秒训练CD
			int soilderType = getSoilderType(buildingRq.getBuildingId());
			RecruitWorkQueCdByGoldEvent recruitWorkQueCdByGoldEvent = new RecruitWorkQueCdByGoldEvent(robot, new RecruitWorkQueCdByGoldAction(), soilderType, 200);
			tryEvent(recruitWorkQueCdByGoldEvent);

			// 5秒之后继续秒
			tryEvent(messageEvent, 5000);
			return;
		}

	}

	private int getSoilderType(int buildingId) {
		if (buildingId == BuildingId.ROCKET_CAMP) {
			return 1;
		} else if (buildingId == BuildingId.TANK_CAMP) {
			return 2;
		} else if (buildingId == BuildingId.WAR_CAR_CAMP) {
			return 3;
		}
		return 1;
	}
}
