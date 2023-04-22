package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BroodWarPb;
import com.game.service.BroodWarService;

/**
 * 兑换母巢商店道具
 */
public class BuyBroodShopHandler extends ClientHandler {

	@Override
	public void action() {
		BroodWarService service = getService(BroodWarService.class);
		BroodWarPb.BuyBroodShopRq req = msg.getExtension(BroodWarPb.BuyBroodShopRq.ext);
		service.buyBroodShopRq(req, this);
	}
}
