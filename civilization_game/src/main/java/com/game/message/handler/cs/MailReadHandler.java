package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class MailReadHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.MailReadRq req = msg.getExtension(MailPb.MailReadRq.ext);
		getService(MailService.class).mailReadRq(req, this);
	}

}
