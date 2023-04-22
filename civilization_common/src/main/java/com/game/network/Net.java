package com.game.network;

import com.game.define.App;
import com.game.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author 陈奎
 * @Description 内部连接
 * @Date 2022/9/9 11:30
 **/

public class Net implements INet {

	// 编号
	protected String id;
	// 应用
	protected App app;
	// 数据包处理器
	protected IPacketHandler packetHandler;
	// 连接上下文
	public ChannelHandlerContext ctx;

	public Net() {
	}

	public Net(ChannelHandlerContext ctx, IPacketHandler packetHandler) {
		this.ctx = ctx;
		this.packetHandler = packetHandler;
	}

	@Override
	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 发送消息
	 *
	 * @param packet
	 */
	public void send(Packet packet) {
		if (ctx == null) {
			return;
		}
		ctx.writeAndFlush(packet);
	}

	@Override
	public void send(Packet packet, ICallback callback) {
		if (ctx == null) {
			return;
		}
		if (callback != null) {
			NetManager.getInst().putCallback(packet, callback);
		}
		ctx.writeAndFlush(packet);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Packet packet) {
		if (!isCallbackMessage(packet)) {
			packetHandler.doPacket(ctx, packet);
		} else {
			NetManager.getInst().doCallback(packet);
		}
	}

	@Override
	public void close() {
		if (ctx != null) {
			ctx.close();
		}
	}

	protected boolean isCallbackMessage(Packet packet) {
		return packet.getSeq() > 0 && packet.getCallBack() == 0;
	}

}
