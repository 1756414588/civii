package com.game.domain;

import com.game.packet.Packet;
import com.game.pb.BasePb.Base;
import com.game.register.PBFile;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author 陈奎
 * @Description 用户客户端
 * @Date 2022/9/9 11:30
 **/

public class UserClient {

	public static final String KEY = "CLIENT-AGENT";
	private long roleId = 0;
	private int accountKey;
	private long channelId;
	private ChannelHandlerContext ctx;
	private long offTime;

	public UserClient(long channelId, ChannelHandlerContext ctx) {
		this.channelId = channelId;
		this.ctx = ctx;
	}

	public void close() {
		ctx.close();
	}

	/**
	 * 给客户端发送消息
	 *
	 * @param packet
	 */
	public void sendPacket(Packet packet) {
		if (ctx != null && !ctx.isRemoved()) {
			ctx.writeAndFlush(packet);
		}
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(int accountKey) {
		this.accountKey = accountKey;
	}

	public long getChannelId() {
		return channelId;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public long getOffTime() {
		return offTime;
	}

	public void setOffTime(long offTime) {
		this.offTime = offTime;
	}
}
