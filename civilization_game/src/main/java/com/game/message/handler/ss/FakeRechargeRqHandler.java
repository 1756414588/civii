package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.FakeRechargeRq;
import com.game.server.GameServer;
import com.game.service.GmToolService;

public class FakeRechargeRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
//		FakeRechargeRq req = msg.getExtension(FakeRechargeRq.ext);
//		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
//		toolService.fakeRecharge(req, this);
	}
}
