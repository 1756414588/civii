package com.game.timer;

import com.game.define.AppTimer;
import com.game.network.RemoteNet;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.HeartBeatRq;
import com.game.server.GateServer;
import com.game.util.BasePbHelper;

/**
 *
 * @Description 心跳定时器
 * @Date 2022/9/9 11:30
 **/

@AppTimer(desc = "网关连接游戏服心跳定时器")
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
		RemoteNet net = (RemoteNet) GateServer.getInst().getNet();
		net.send(packet);
	}


}
