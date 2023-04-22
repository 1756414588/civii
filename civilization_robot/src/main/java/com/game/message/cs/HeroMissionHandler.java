package com.game.message.cs;

import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

/**
 * 副本招募武将
 */
public class HeroMissionHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext cx, int accountKey, Base req) {
	}
}
