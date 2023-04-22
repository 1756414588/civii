package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.SoldierPb.RecruitWorkQueCdRq;
import com.game.util.BasePbHelper;

/**
 * @Author 陈奎
 * @Description金币秒士兵招CD
 * @Date 2022/9/27 15:21
 **/

public class RecruitWorkQueCdByGoldEvent extends MessageEvent {

	private int soldierType;

	public RecruitWorkQueCdByGoldEvent(Robot robot, IAction action, int soldierType, long delayTime) {
		super(robot, action, delayTime);
		this.soldierType = soldierType;
	}

	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		RecruitWorkQueCdRq.Builder builder = RecruitWorkQueCdRq.newBuilder();
		builder.setCost(1);// 使用金币秒
		builder.setSoldierType(soldierType);
		Base base = BasePbHelper.createRqBase(RecruitWorkQueCdRq.EXT_FIELD_NUMBER, eventId, RecruitWorkQueCdRq.ext, builder.build()).build();
		return PacketCreator.create(base);
	}
}
