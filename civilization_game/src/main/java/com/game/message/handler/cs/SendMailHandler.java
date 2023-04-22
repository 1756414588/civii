package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class SendMailHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.SendMailRq req = msg.getExtension(MailPb.SendMailRq.ext);
		getService(MailService.class).sendMailRq(req, this);
	}

}
