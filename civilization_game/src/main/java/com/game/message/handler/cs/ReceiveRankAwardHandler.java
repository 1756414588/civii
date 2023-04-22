package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/3 17:57
 **/
public class ReceiveRankAwardHandler extends ClientHandler {
	@Override
	public void action() {
		getService(TDService.class).receiveRankAwardRq(this);
	}
}
