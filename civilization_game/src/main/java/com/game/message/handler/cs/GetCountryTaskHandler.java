package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CountryService;

public class GetCountryTaskHandler extends ClientHandler {
	@Override
	public void action() {
		getService(CountryService.class).getCountryTaskRq(this);
	}
}