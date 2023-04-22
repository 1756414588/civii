package com.game.acion.events;

import com.game.acion.EventAction;
import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.AddSoldierRq;
import com.game.server.TimerServer;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 补兵事件
 * @Date 2022/9/21 20:58
 **/

public class AddSoldierEvent extends MessageEvent {

	private int heroId;

	public AddSoldierEvent(Robot robot, int heroId, long delayTime) {
		super(robot, null, delayTime);
		this.heroId = heroId;
		this.initAction();
	}

	private void initAction() {
		EventAction eventAction = new EventAction();
		eventAction.setMessageEvent(this);
		this.action = eventAction;
	}


	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		AddSoldierRq.Builder builder = AddSoldierRq.newBuilder();
		builder.setHeroId(heroId);
		Base base = BasePbHelper.createRqBase(AddSoldierRq.EXT_FIELD_NUMBER, eventId, AddSoldierRq.ext, builder.build()).build();
		return PacketCreator.create(base);
	}


	@Override
	public void action() {
		try {
			action.doAction(this, this.getRobot());
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public void complate(Robot robot, Base base) {

		// 总带兵量不足
		if (base.getCode() == GameError.NO_SOLDIER_COUNT.getCode()) {
			AddSoldierNumberEvent addSoldierNumberEvent = new AddSoldierNumberEvent(robot, new EventAction(), heroId, 100);
			TimerServer.getInst().addDelayEvent(addSoldierNumberEvent);

			// 补充完兵量,继续给武将补兵
			this.reset(100);
			action.registerEvent(robot);
		}
	}


}
