package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.ForbiddenRq;
import com.game.server.GameServer;
import com.game.service.GmToolService;

public class ForbiddenRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
//		ForbiddenRq req = msg.getExtension(ForbiddenRq.ext);
//
//		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
//		toolService.forbidden(req, this);
	}
}
