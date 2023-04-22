package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb.BlackListRq;
import com.game.service.MailService;

public class BlackListHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		BlackListRq req = msg.getExtension(BlackListRq.ext);
		getService(MailService.class).blackList(req, this);
	}

}
