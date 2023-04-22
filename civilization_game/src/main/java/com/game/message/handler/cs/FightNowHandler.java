package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.message.handler.DealType;
import com.game.pb.BroodWarPb;
import com.game.pb.BroodWarPb.FightNowRq;
import com.game.server.GameServer;
import com.game.server.ICommand;
import com.game.service.BroodWarService;
import com.game.util.LogHelper;

/**
 * @author zcp
 * @date 2021/7/5 9:29
 */
public class FightNowHandler extends ClientHandler {

	@Override
	public void action() {
		BroodWarService service = getService(BroodWarService.class);
		BroodWarPb.FightNowRq req = msg.getExtension(BroodWarPb.FightNowRq.ext);
		FightNowAction action = new FightNowAction(service, req, this);
		GameServer.getInstance().mainLogicServer.addCommand(action, DealType.MAIN);
	}

	public class FightNowAction implements ICommand {

		private BroodWarService service;
		private BroodWarPb.FightNowRq req;
		private ClientHandler handler;

		public FightNowAction(BroodWarService service, FightNowRq req, ClientHandler handler) {
			this.service = service;
			this.req = req;
			this.handler = handler;
		}

		@Override
		public void action() {
			try {
				service.fightNow(req, handler);
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
