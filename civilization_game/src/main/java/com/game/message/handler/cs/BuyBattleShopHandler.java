package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb.BuyBattleShopRq;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/29 14:16
 **/
public class BuyBattleShopHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).buyBattleShopRq(this, msg.getExtension(BuyBattleShopRq.ext));
	}
}
