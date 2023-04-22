package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.DepotPb.BuyDepotRq;
import com.game.service.DepotService;

public class BuyDepotHandler extends ClientHandler {
	@Override
	public void action() {
		DepotService service = getService(DepotService.class);
		BuyDepotRq req = msg.getExtension(BuyDepotRq.ext);
		service.buyDepotRq(req, this);
	}
}