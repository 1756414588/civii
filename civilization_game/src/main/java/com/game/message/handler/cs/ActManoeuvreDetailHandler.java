package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreDetailRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreDetailHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreDetailRq req = msg.getExtension(ActManoeuvreDetailRq.ext);
		service.actManoeuvreDetailRq(req, this);
	}
}
