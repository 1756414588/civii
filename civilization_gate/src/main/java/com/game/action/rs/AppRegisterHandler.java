package com.game.action.rs;

import com.game.action.PacketHandler;
import com.game.network.RemoteNet;
import com.game.packet.Packet;
import com.game.pb.InnerPb.RegisterRs;
import com.game.server.GateServer;
import io.netty.channel.ChannelHandlerContext;

public class AppRegisterHandler extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
		RemoteNet net = (RemoteNet) GateServer.getInst().getNet();
		RegisterRs rps = getMsg(packet, RegisterRs.ext);
		if (rps.getState() == 1) {
			net.setId(rps.getServerId());
		}
	}
}
