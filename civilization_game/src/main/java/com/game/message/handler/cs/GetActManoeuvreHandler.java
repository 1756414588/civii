package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActManoeuvreService;


public class GetActManoeuvreHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		service.getActManoeuvreRq(this);
	}
}
