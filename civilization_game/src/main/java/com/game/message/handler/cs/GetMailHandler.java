package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MailPb;
import com.game.service.MailService;

public class GetMailHandler extends ClientHandler{

	/** 
	* Overriding: action    
	* @see com.game.server.ICommand#action()    
	*/
	@Override
	public void action() {
		// TODO Auto-generated method stub
        MailPb.GetMailRq req = msg.getExtension(MailPb.GetMailRq.ext);
        getService(MailService.class).getMailRq(req, this);
	}

}
