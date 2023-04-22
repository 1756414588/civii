package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class ReadAllHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.ReadAllRq req = msg.getExtension(MailPb.ReadAllRq.ext);
		getService(MailService.class).readAllRq(req, this);
	}

}
