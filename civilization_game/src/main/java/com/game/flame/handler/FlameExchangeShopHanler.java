package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

/**
 * 商店兑换
 */
public class FlameExchangeShopHanler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).exchangeShop(this, msg.getExtension(FlameWarPb.ShopExchangeRq.ext));
	}
}
