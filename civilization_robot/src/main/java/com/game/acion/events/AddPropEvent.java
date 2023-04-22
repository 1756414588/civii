package com.game.acion.events;

import com.game.acion.EventAction;
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
 * @Description 添加道具事件
 * @Date 2022/9/27 15:03
 **/

public class AddPropEvent extends MessageEvent {

	private int propId;
	private int count;

	public AddPropEvent(Robot robot, int propId, int count, long delayTime) {
		super(robot, null, delayTime);
		this.propId = propId;
		this.count = count;
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
		RobotRepairRq.Builder builder = RobotRepairRq.newBuilder();
		builder.setCipherCode("zzhy666");
		CommonPb.Award.Builder awardPb = CommonPb.Award.newBuilder();
		awardPb.setType(AwardType.PROP);
		awardPb.setId(propId);
		awardPb.setCount(count);
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

	}


}
