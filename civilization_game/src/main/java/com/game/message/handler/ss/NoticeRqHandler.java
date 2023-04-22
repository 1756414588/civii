package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.NoticeRq;
import com.game.server.GameServer;
import com.game.service.GmToolService;

public class NoticeRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
//		NoticeRq req = msg.getExtension(NoticeRq.ext);
//
//		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
//		toolService.sendNotice(req, this);
	}
}
