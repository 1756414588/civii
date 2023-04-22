package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreArmyRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreArmyHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreArmyRq req = msg.getExtension(ActManoeuvreArmyRq.ext);
		service.actManoeuvreArmyRq(req, this);
	}
}
