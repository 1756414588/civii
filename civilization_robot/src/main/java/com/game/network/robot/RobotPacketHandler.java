package com.game.network.robot;

import com.game.packet.Packet;
import com.game.network.IPacketHandler;
import com.game.server.exec.MessageExecutor;
import com.game.server.work.MessageTask;
import com.game.spring.SpringUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 机器人的包处理器
 */
public class RobotPacketHandler implements IPacketHandler {


	@Override
	public void doPacket(ChannelHandlerContext ctx, Packet packet) {
		MessageExecutor messageExecutor = SpringUtil.getBean(MessageExecutor.class);
		messageExecutor.add(new MessageTask(ctx, packet));
	}
}
