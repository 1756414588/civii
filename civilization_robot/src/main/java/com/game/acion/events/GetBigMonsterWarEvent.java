package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.GetBigMonsterWarRq;
import com.game.util.BasePbHelper;

/**
 * @Author 陈奎
 * @Description 拉取巨型虫族战斗数据
 * @Date 2022/10/27 10:22
 **/

public class GetBigMonsterWarEvent extends MessageEvent {

	public GetBigMonsterWarEvent(Robot robot, IAction action, long delayTime) {
		super(robot, action, delayTime);
	}

	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		GetBigMonsterWarRq.Builder builder = GetBigMonsterWarRq.newBuilder();
		Base base = BasePbHelper.createRqBase(GetBigMonsterWarRq.EXT_FIELD_NUMBER, eventId, GetBigMonsterWarRq.ext, builder.build()).build();
		return PacketCreator.create(base);
	}
}
