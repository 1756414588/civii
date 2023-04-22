package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb.ReplyMailRq;
import com.game.service.MailService;

public class ReplyMailHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		// TODO Auto-generated method stub
		ReplyMailRq req = msg.getExtension(ReplyMailRq.ext);
		getService(MailService.class).replyMailRq(req, this);
	}

}
