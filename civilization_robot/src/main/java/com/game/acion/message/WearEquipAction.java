package com.game.acion.message;

import com.game.acion.EventAction;
import com.game.acion.MessageAction;
import com.game.acion.MessageEvent;
import com.game.acion.events.GetEquipBagEvent;
import com.game.constant.GameError;
import com.game.domain.BagEquip;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.manager.GuildeManager;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.EquipPb.WearEquipRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 穿戴装备
 * @Date 2022/9/14 19:04
 **/

public class WearEquipAction extends MessageAction {

	private WearEquipRq wearEquipRq;

	public WearEquipAction(RobotMessage robotMessage) {
		super(robotMessage);
		this.wearEquipRq = BasePbHelper.createPb(WearEquipRq.ext, robotMessage.getContent());
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		long eventId = messageEvent.getEventId();
		int heroId = wearEquipRq.getHeroId();
		int equipId = wearEquipRq.getEquipId();

		UserBagCache userBag = robot.getCache().getBagCache();

		// 获取对应的装备
		BagEquip bagEquip = userBag.getEquipById(equipId);
		if (bagEquip == null) {
			LogHelper.CHANNEL_LOGGER.info("装备不存在 robotId:{} id:{} heroId:{} equipId:{}", this.id, robot.getId(), wearEquipRq.getHeroId(), wearEquipRq.getEquipId());
			return;
		}

		WearEquipRq.Builder builder = WearEquipRq.newBuilder();
		builder.setHeroId(heroId);
		builder.setKeyId(bagEquip.getKeyId());
		builder.setEquipId(bagEquip.getEquipId());

		Base.Builder base = BasePbHelper.createRqBase(WearEquipRq.EXT_FIELD_NUMBER, eventId, WearEquipRq.ext, builder.build());
		robot.sendPacket(PacketCreator.create(base.build()));

		LogHelper.CHANNEL_LOGGER.info("[消息.发送] accountKey:{} cmd:{} eventId:{} id:{} name:{} heroId:{} equipId:{}", robot.getId(), requestCode, eventId, id, getName(), wearEquipRq.getHeroId(), wearEquipRq.getEquipId());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		LogHelper.CHANNEL_LOGGER.info("[消息.返回] accountKey:{} cmd:{} eventId:{} id:{} code:{}", robot.getId(), base.getCommand(), base.getParam(), id, base.getCode());

		// 新手引导已经做完了，没有相关任务硬性要求，装备没有则直接跳过
		GuildeManager guildeManager = getBean(GuildeManager.class);
		if (guildeManager.isComplate(robot)) {// 新手引导已做完
			RobotData robotData = robot.getData();
			robotData.setGuildState(robotData.getGuildState() + 1);
			return;
		}

		// 装备部不存在
		if (base.getCode() != GameError.OK.getCode()) {
			// 刷新装备背包
			GetEquipBagEvent getEquipBagEvent = new GetEquipBagEvent(robot, new EventAction(), 100);
			tryEvent(getEquipBagEvent);

			tryEvent(messageEvent, 2000);
			return;
		}

		RobotData robotData = robot.getData();
		robotData.setGuildState(robotData.getGuildState() + 1);
	}
}
