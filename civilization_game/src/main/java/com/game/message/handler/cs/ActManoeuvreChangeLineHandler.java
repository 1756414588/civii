package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreChangeLineRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreChangeLineHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreChangeLineRq req = msg.getExtension(ActManoeuvreChangeLineRq.ext);
		service.actManoeuvreChangeLineRq(req, this);
	}
}
