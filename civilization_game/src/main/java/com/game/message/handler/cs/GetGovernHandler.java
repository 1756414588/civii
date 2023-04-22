package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CountryService;

public class GetGovernHandler extends ClientHandler {
	@Override
	public void action() {
		getService(CountryService.class).getGovernRq(this);
	}
}