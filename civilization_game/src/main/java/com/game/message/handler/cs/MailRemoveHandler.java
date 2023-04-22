package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class MailRemoveHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.MailRemoveRq req = msg.getExtension(MailPb.MailRemoveRq.ext);
		getService(MailService.class).mailRemoveRq(req, this);
	}

}
