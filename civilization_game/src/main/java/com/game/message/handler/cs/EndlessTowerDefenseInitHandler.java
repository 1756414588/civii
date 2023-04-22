package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/25 17:05
 **/
public class EndlessTowerDefenseInitHandler extends ClientHandler {

	@Override
	public void action() {
		getService(TDService.class).endlessTowerDefenseInitRq(this);
	}
}
