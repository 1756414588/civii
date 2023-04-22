package com.game.action;

import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

public interface IPacketHandler {

	void action(ChannelHandlerContext ctx, Packet req);

	void setResponseCmd(int responseCmd);

}
