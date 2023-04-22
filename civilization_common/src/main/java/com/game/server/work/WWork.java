package com.game.server.work;

import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

public class WWork extends AbstractWork {

	private ChannelHandlerContext ctx;
	private Base msg;

	public WWork(ChannelHandlerContext ctx, Base msg) {
		this.ctx = ctx;
		this.msg = msg;
	}

	/**
	 * Overriding: run
	 *
	 * @see Runnable#run()
	 */
	@Override
	public void run() {
		try {
			ctx.channel().writeAndFlush(msg);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("WWork", e);
		}
	}
}
