package com.game.network;

import com.game.action.MessagePool;
import com.game.domain.UserClient;
import com.game.manager.UserClientManager;
import com.game.manager.RobotManager;
import com.game.packet.Packet;
import com.game.server.GateServer;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Author 陈奎
 * @Description 玩家消息处理器
 * @Date 2022/9/9 11:30
 **/


public class MessageHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Packet packet = (Packet) msg;

		if (!GateServer.getInst().ready) {
			LogHelper.CHANNEL_LOGGER.info("连接未开启 cmd:{}", packet.getCmd());
			ctx.close();
			return;
		}

		if (MessagePool.getInst().isContain(packet.getCmd())) {
			MessagePool.getInst().handler(ctx, packet);
			return;
		}

		GateServer.getInst().getNet().send(packet);

		// 监听玩家请求协议
		RobotManager.getInst().getListenRobotList().forEach(e -> {
			e.listenPacket(packet);
		});
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		int total = UserClientManager.maxConnect.get();
		if (total > UserClientManager.MAX_CONNECT) {
			return;
		} else {
			UserClientManager.maxConnect.incrementAndGet();
			ChannelUtil.setHeartTime(ctx, System.currentTimeMillis());
		}
		Long index = ChannelUtil.createChannelId(ctx);
		ChannelUtil.setChannelId(ctx, index);
		ChannelUtil.setRoleId(ctx, 0L);
		UserClient userClient = new UserClient(index, ctx);
		ChannelUtil.setAttribute(ctx, UserClient.KEY, userClient);
		UserClientManager.getInst().put(index, userClient);
		LogHelper.CHANNEL_LOGGER.info("channelActive channelId:{}", index);

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		UserClientManager.maxConnect.decrementAndGet();
		long roleId = ChannelUtil.getRoleId(ctx);
		long channelId = ChannelUtil.getChannelId(ctx);
		UserClientManager.getInst().offLine(channelId, roleId);
		UserClientManager.getInst().remove(channelId);
		LogHelper.CHANNEL_LOGGER.info("连接断开 channelId:{} playerId:{}", channelId, roleId);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		LogHelper.CHANNEL_LOGGER.info("发生异常连接断开", cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				ctx.close();
				LogHelper.CHANNEL_LOGGER.info("触发心跳 channelId:{}", ChannelUtil.getChannelId(ctx));
			}
		}
	}

}
