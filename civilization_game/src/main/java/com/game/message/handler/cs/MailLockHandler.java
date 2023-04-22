package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class MailLockHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.MailLockRq req = msg.getExtension(MailPb.MailLockRq.ext);
		getService(MailService.class).mailLockRq(req, this);
	}

}
