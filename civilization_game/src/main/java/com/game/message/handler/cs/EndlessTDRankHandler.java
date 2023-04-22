package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/26 9:25
 **/
public class EndlessTDRankHandler extends ClientHandler {
	@Override
	public void action() {
		getService(TDService.class).endlessTDRankRq(this);
	}
}
