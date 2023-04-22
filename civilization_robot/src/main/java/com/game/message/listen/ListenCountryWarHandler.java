package com.game.message.listen;

import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 发起国战
 */
public class ListenCountryWarHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
//		LogHelper.CHANNEL_LOGGER.info("发起国战 msg:{}", req);
	}

}
