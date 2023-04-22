package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.ModVipRq;
import com.game.server.GameServer;
import com.game.service.GmToolService;

public class ModVipRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
//		ModVipRq req = msg.getExtension(ModVipRq.ext);
//
//		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
//		toolService.modVip(req, this);
	}
}
