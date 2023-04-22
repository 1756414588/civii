package com.game.timer;

import com.game.define.AppTimer;
import com.game.manager.RobotNetManager;
import com.game.network.NetManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.HeartBeatRq;
import com.game.spring.SpringUtil;
import com.game.util.BasePbHelper;

@AppTimer(desc = "机器人心跳")
public class HeartBeatTimer extends TimerEvent {

	private Packet packet;

	public HeartBeatTimer() {
		super(-1, 1000);
		this.packet = initPacket();
	}

	private Packet initPacket() {
		HeartBeatRq.Builder req = HeartBeatRq.newBuilder();
		Base.Builder base = BasePbHelper.createRqBase(HeartBeatRq.EXT_FIELD_NUMBER, HeartBeatRq.ext, req.build());
		return PacketCreator.create(HeartBeatRq.EXT_FIELD_NUMBER, base.build().toByteArray());
	}

	@Override
	public void action() {
		long current = System.currentTimeMillis();
		RobotNetManager robotNetManager = SpringUtil.getBean(RobotNetManager.class);
		robotNetManager.getNetStateMap().values().stream().filter(e -> e.getHeatBeatTime() <= current).forEach(e -> {
			e.getNet().send(packet);
			e.setHeatBeatTime(current + 60000);
		});
	}
}
