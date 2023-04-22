package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.EquipPb.GetEquipBagRq;
import com.game.util.BasePbHelper;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/28 8:50
 **/

public class GetEquipBagEvent extends MessageEvent {

	public GetEquipBagEvent(Robot robot, IAction action, long delayTime) {
		super(robot, action, delayTime);
	}


	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		GetEquipBagRq.Builder builder = GetEquipBagRq.newBuilder();
		Base base = BasePbHelper.createRqBase(GetEquipBagRq.EXT_FIELD_NUMBER, eventId, GetEquipBagRq.ext, builder.build()).build();
		return PacketCreator.create(base);
	}
}
