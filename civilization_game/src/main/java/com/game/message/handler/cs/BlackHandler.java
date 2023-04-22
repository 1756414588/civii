package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb.BlackRq;
import com.game.service.MailService;

public class BlackHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		BlackRq req = msg.getExtension(BlackRq.ext);
		getService(MailService.class).blackRq(req, this);
	}

}
