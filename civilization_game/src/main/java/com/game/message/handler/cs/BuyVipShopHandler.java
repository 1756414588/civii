package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ShopPb.BuyVipShopRq;
import com.game.service.ShopService;

public class BuyVipShopHandler extends ClientHandler {
	@Override
	public void action() {
		ShopService service = getService(ShopService.class);
		BuyVipShopRq req = msg.getExtension(BuyVipShopRq.ext);
		service.buyVipShopRq(req, this);
	}
}