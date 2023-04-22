package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ShopService;

public class GetVipGiftHandler extends ClientHandler {
	@Override
	public void action() {
		ShopService service = getService(ShopService.class);
		service.getVipGiftRq(this);
	}
}