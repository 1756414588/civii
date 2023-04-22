package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class GetMailReportHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		MailPb.GetMailReportRq req = msg.getExtension(MailPb.GetMailReportRq.ext);
		getService(MailService.class).getMailReportRq(req, this);
	}

}
