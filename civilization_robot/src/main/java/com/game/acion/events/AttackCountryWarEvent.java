package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.AttendCountryWarRq;
import com.game.util.BasePbHelper;

/**
 *
 * @Description 参加国战信息
 * @Date 2022/10/27 10:22
 **/

public class AttackCountryWarEvent extends MessageEvent {

	private int warId;
	private int mapId;

	public AttackCountryWarEvent(Robot robot, IAction action, long delayTime, int warId, int mapId) {
		super(robot, action, delayTime);
		this.warId = warId;
		this.mapId = mapId;
	}

	/**
	 * 创建消息包
	 *
	 * @return
	 */
	@Override
	public Packet createPacket() {
		AttendCountryWarRq.Builder builder = AttendCountryWarRq.newBuilder();
		builder.setWarId(warId);
		builder.setMapId(mapId);
		builder.addAllHeroId(robot.getCache().getHeroCache().getEmbattles());
		Base base = BasePbHelper.createRqBase(AttendCountryWarRq.EXT_FIELD_NUMBER, eventId, AttendCountryWarRq.ext, builder.build()).build();
		return PacketCreator.create(base);
	}
}
