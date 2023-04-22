package com.game.network;

import com.game.define.App;
import com.game.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @Description 内部连接接口
 * @Date 2022/9/9 11:30
 **/

public interface INet {

	App getApp();

	String getId();

	void send(Packet packet);

	void send(Packet packet, ICallback callback);

	void messageReceived(ChannelHandlerContext ctx, Packet packet);

	void close();

}
