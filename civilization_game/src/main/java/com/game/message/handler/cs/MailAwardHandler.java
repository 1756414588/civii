package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class MailAwardHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.MailAwardRq req = msg.getExtension(MailPb.MailAwardRq.ext);
		getService(MailService.class).mailAwardRq(req, this);
	}

}
