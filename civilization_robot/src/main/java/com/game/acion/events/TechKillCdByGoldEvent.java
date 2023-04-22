package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.TechPb.TechKillCdRq;
import com.game.pb.WorldPb.AddSoldierRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 *
 * @Description 砖石秒科技CD
 * @Date 2022/9/21 20:58
 **/

public class TechKillCdByGoldEvent extends MessageEvent {

	public TechKillCdByGoldEvent(Robot robot, IAction action, long delayTime) {
		super(robot, action, delayTime);
	}

	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		TechKillCdRq.Builder builder = TechKillCdRq.newBuilder();
		builder.setCost(2);
		Base base = BasePbHelper.createRqBase(TechKillCdRq.EXT_FIELD_NUMBER, eventId, TechKillCdRq.ext, builder.build()).build();
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
		// TODO 这里可以上次未完成的事件继续完成,或者完成事件组中的下一个事件
	}


}
