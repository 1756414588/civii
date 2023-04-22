package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.GameMailRq;
import com.game.server.GameServer;
import com.game.service.GmToolService;

public class GameMailRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
//		GameMailRq req = msg.getExtension(GameMailRq.ext);
//
//		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
//		toolService.sendMail(req, this);

	}
}
