package com.game.network;

import com.game.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author 陈奎
 * @Description 内部连接packet包处理器
 * @Date 2022/9/9 11:30
 **/

public interface IPacketHandler {

	void doPacket(ChannelHandlerContext ctx, Packet packet);

}
