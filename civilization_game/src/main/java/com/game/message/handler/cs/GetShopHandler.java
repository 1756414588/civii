package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ShopService;

public class GetShopHandler extends ClientHandler {
	@Override
	public void action() {
		ShopService service = getService(ShopService.class);
		service.getShopRq(this);
	}
}