package com.game.uc.netty.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.game.pb.BasePb;
import com.google.protobuf.GeneratedMessage;

import io.netty.channel.ChannelHandlerContext;

public abstract class BaseHandler {

	@PostConstruct
	public abstract void register();

	public static final Map<Integer, BaseHandler> map = new ConcurrentHashMap<>();

	protected void addHandler(int cmd, BaseHandler handler) {
		map.put(cmd, handler);
	}

	public abstract void action(ChannelHandlerContext context, BasePb.Base msg);

	protected <T> void sendAndFlush(ChannelHandlerContext ctx, GeneratedMessage.GeneratedExtension<BasePb.Base, T> ext, T msg, int rsId) {
		BasePb.Base.Builder send = BasePb.Base.newBuilder();
		send.setExtension(ext, msg);
		send.setCommand(rsId);
		ctx.writeAndFlush(send.build());
	}
}
