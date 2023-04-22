package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ShopPb.BuyVipGiftRq;
import com.game.service.ShopService;

public class BuyVipGiftHandler extends ClientHandler {
	@Override
	public void action() {
		BuyVipGiftRq req = msg.getExtension(BuyVipGiftRq.ext);
		ShopService service = getService(ShopService.class);
		service.buyVipGiftRq(req, this);
	}
}