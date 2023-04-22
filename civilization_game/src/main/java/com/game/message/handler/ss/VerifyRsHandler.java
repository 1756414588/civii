package com.game.message.handler.ss;

import io.netty.channel.ChannelHandlerContext;

import com.game.message.handler.ServerHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.VerifyRs;
import com.game.pb.RolePb.UserLoginRs;
import com.game.server.GameServer;
import com.game.server.work.WWork;
import com.game.service.AccountService;

public class VerifyRsHandler extends ServerHandler {
	@Override
	public void action() {
		VerifyRs req = msg.getExtension(VerifyRs.ext);
		GameServer gameServer = GameServer.getInstance();
		Long channelId = req.getChannelId();
		ChannelHandlerContext ctx = gameServer.userChannels.get(channelId);

		if (ctx == null) {
			return;
		}

		if (msg.getCode() != 200) {
			Base.Builder builder = Base.newBuilder();
			builder.setCommand(UserLoginRs.EXT_FIELD_NUMBER);
			builder.setCode(msg.getCode());
			//gameServer.connectServer.sendExcutor.addTask(channelId, new WWork(ctx, builder.build()));
			return;
		}

//		AccountService accountService = SpringUtil.getBean(AccountService.class);
//		accountService.verifyRs(req, this, ctx);
	}
}
