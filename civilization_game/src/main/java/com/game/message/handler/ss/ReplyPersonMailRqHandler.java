package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.ReplyPersonMailRq;
import com.game.service.GmToolService;
import com.game.spring.SpringUtil;

public class ReplyPersonMailRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		ReplyPersonMailRq req = msg.getExtension(ReplyPersonMailRq.ext);

		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
		toolService.replyPersonMailRq(req, this);

	}
}
