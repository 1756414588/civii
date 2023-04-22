package com.game.message;

import com.game.pb.BasePb.Base;
import io.netty.channel.ChannelHandlerContext;

public interface IMessageHandler {

	void action(ChannelHandlerContext ctx, int accountKey, Base base);

	void setResponseCmd(int responseCmd);

}
