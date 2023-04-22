package com.game.client;

import com.game.pb.BasePb.Base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<Base> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Base msg) throws Exception {

	}
}
