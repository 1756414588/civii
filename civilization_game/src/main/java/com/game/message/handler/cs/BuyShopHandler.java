package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ShopPb.BuyShopRq;
import com.game.service.ShopService;

public class BuyShopHandler extends ClientHandler {
	@Override
	public void action() {
		ShopService service = getService(ShopService.class);
		BuyShopRq req = msg.getExtension(BuyShopRq.ext);
		service.buyShopRq(req, this);
	}
}