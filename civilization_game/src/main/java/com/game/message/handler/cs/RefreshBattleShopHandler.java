package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/6 14:58
 **/
public class RefreshBattleShopHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).refreshBattleShopRq(this);
	}
}
