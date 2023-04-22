package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.DepotPb.ExchangeResRq;
import com.game.service.DepotService;

public class ExchangeResHandler extends ClientHandler {
	@Override
	public void action() {
		DepotService service = getService(DepotService.class);
		ExchangeResRq req = msg.getExtension(ExchangeResRq.ext);
		service.exchangeResRq(req, this);
	}
}