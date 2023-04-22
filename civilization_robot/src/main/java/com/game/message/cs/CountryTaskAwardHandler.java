package com.game.message.cs;

import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

/**
 * 领取国家奖励
 */
public class CountryTaskAwardHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

	}
}
