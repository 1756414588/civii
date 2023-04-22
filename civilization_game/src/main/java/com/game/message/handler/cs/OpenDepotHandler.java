package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.DepotPb.OpenDepotRq;
import com.game.service.DepotService;

public class OpenDepotHandler extends ClientHandler {
	@Override
	public void action() {
		DepotService service = getService(DepotService.class);
		OpenDepotRq req = msg.getExtension(OpenDepotRq.ext);
		service.openDepotRq(req, this);
	}
}