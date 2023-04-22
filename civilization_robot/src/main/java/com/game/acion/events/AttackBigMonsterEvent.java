package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.AttackRebelRq;
import com.game.pb.WorldPb.AttendCountryWarRq;
import com.game.util.BasePbHelper;

/**
 *
 * @Description 攻打巨型虫族
 * @Date 2022/10/27 10:22
 **/

public class AttackBigMonsterEvent extends MessageEvent {

	private int posX;
	private int posY;

	public AttackBigMonsterEvent(Robot robot, IAction action, long delayTime, int posX, int posY) {
		super(robot, action, delayTime);
		this.posX = posX;
		this.posY = posY;
	}

	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		AttackRebelRq.Builder builder = AttackRebelRq.newBuilder();
		builder.setPos(Pos.newBuilder().setX(posX).setY(posY).build());
		builder.addAllHeroId(robot.getCache().getHeroCache().getEmbattles());
		Base base = BasePbHelper.createRqBase(AttackRebelRq.EXT_FIELD_NUMBER, eventId, AttackRebelRq.ext, builder.build()).build();
		return PacketCreator.create(base);
	}
}
