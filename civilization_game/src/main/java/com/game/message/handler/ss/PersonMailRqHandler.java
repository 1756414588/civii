package com.game.message.handler.ss;

import com.game.message.handler.ServerHandler;
import com.game.pb.GmToolPb.PersonMailRq;
import com.game.service.GmToolService;
import com.game.spring.SpringUtil;

public class PersonMailRqHandler extends ServerHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		PersonMailRq req = msg.getExtension(PersonMailRq.ext);

		GmToolService toolService = SpringUtil.getBean(GmToolService.class);
		toolService.personMailRq(req, this);

	}
}
