package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/19 17:35
 **/
public class GetAllMainCityCountryHandler extends ClientHandler {

	@Override
	public void action() {
		getService(WorldService.class).getAllMainCityCountryRq(this);
	}
}
