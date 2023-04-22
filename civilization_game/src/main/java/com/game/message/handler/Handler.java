package com.game.message.handler;

import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import io.netty.channel.ChannelHandlerContext;

import com.game.constant.GameError;
import com.game.pb.BasePb.Base;
import com.game.server.ICommand;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.net.InetSocketAddress;


abstract public class Handler implements ICommand {

	static public final int PUBLIC = 0;
	static public final int MAIN = 1;
	static public final int BUILD_QUE = 2;
	static public final int TANK_QUE = 3;

	private int rsMsgCmd;
	protected ChannelHandlerContext ctx;
	protected Base msg;
	protected long channelId;
	protected long createTime;

	public Handler(ChannelHandlerContext ctx, Base msg) {
		this.ctx = ctx;
		this.msg = msg;
		setCreateTime(System.currentTimeMillis());
	}

	public Handler() {
		setCreateTime(System.currentTimeMillis());
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public Base getMsg() {
		return msg;
	}

	public void setMsg(Base msg) {
		this.msg = msg;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public <T> Base.Builder createRsBase(GameError gameError, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(rsMsgCmd);
		baseBuilder.setCode(gameError.getCode());
		if (this.msg.hasParam()) {
			baseBuilder.setParam(this.msg.getParam());
		}

		if (this.msg.hasIndex()) {
			baseBuilder.setIndex(this.msg.getIndex());
		}

		if (msg != null) {
			baseBuilder.setExtension(ext, msg);
		}

		return baseBuilder;
	}

	public Base.Builder createRsBase(int code) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(rsMsgCmd);
		baseBuilder.setCode(code);
		if (this.msg.hasParam()) {
			baseBuilder.setParam(this.msg.getParam());
		}

		if (this.msg.hasIndex()) {
			baseBuilder.setIndex(this.msg.getIndex());
		}

		return baseBuilder;
	}

	public <T> T getService(Class<T> c) {
		return SpringUtil.getBean(c);
	}

	public Long getChannelId() {
		return channelId;
	}

	abstract public DealType dealType();

	public int getRsMsgCmd() {
		return rsMsgCmd;
	}

	public void setRsMsgCmd(int rsMsgCmd) {
		this.rsMsgCmd = rsMsgCmd;
	}

	public long getIndex() {
		if (msg.hasIndex()) {
			return msg.getIndex();
		}
		return 0;
	}

	public String getIpAddress() {
		String clientIp = "";
		try {
			InetSocketAddress ipSocket = (InetSocketAddress) this.ctx.channel().remoteAddress();
			clientIp = ipSocket.getAddress().getHostAddress();
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
		return clientIp;
	}
}
