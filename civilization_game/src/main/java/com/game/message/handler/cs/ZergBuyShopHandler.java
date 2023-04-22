package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.ZergPb.ZergBuyShopRq;
import com.game.service.ZergService;

public class ZergBuyShopHandler extends ClientHandler {

	@Override
	public void action() {
		ZergService service = getService(ZergService.class);
		ZergBuyShopRq req = msg.getExtension(ZergBuyShopRq.ext);
		service.ZergBuyShopRq(req, this);
	}
}

