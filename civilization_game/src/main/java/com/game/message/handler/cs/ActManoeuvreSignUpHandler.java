package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreSignUpRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreSignUpHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreSignUpRq req = msg.getExtension(ActManoeuvreSignUpRq.ext);
		service.actManoeuvreSignUpRq(req, this);
	}
}
