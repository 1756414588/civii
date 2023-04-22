package com.game.network;

import com.game.packet.Packet;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *
 * @Description 连接句柄
 * @Date 2022/9/9 11:30
 **/

public class ChannelHandler extends ChannelInboundHandlerAdapter {

	protected RemoteNet net;
	protected INetContext connect;

	public ChannelHandler(RemoteNet net, INetContext connect) {
		this.net = net;
		this.connect = connect;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Packet) {
			net.messageReceived(ctx, (Packet) msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		LogHelper.CHANNEL_LOGGER.error("发生异常连接断开 {}:{} ", connect.host(), connect.port(), cause);
	}

	/**
	 * 连接成功
	 *
	 * @param ctx
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		net.ctx = ctx;
		LogHelper.CHANNEL_LOGGER.info("连接远程成功 {}:{}", connect.host(), connect.port());
		ChannelUtil.setNetId(ctx);
		connect.onSucess(ctx);
	}

	/**
	 * 连接断开,则发起重连
	 *
	 * @param ctx
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		connect.onDisconnect(ctx);
		net.ctx = null;
		LogHelper.CHANNEL_LOGGER.info("远程断开连接 {}:{}", connect.host(), connect.port());
		net.connectToServer();
	}
}
