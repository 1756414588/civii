package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.constant.AwardType;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb;
import com.game.pb.MapInfoPb.RobotRepairRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

/**
 * @Author 陈奎
 * @Description 添加砖石事件
 * @Date 2022/9/21 20:58
 **/

public class AddGoldEvent extends MessageEvent {

	private int gold;

	public AddGoldEvent(Robot robot, IAction action, int addGold, long delayTime) {
		super(robot, action, delayTime);
		this.gold = addGold;
	}

	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		RobotRepairRq.Builder builder = RobotRepairRq.newBuilder();
		builder.setCipherCode("zzhy666");
		CommonPb.Award.Builder awardPb = CommonPb.Award.newBuilder();
		awardPb.setType(AwardType.GOLD);
		awardPb.setId(0);
		awardPb.setCount(gold);
		builder.addAwards(awardPb);
		Base base = BasePbHelper.createRqBase(RobotRepairRq.EXT_FIELD_NUMBER, eventId, RobotRepairRq.ext, builder.build()).build();
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
