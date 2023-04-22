package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.CountryService;

public class GetCountryGloryHandler extends ClientHandler {
	@Override
	public void action() {
		// CountryPb.CountryTaskAwardRq req =
		// msg.getExtension(CountryPb.CountryTaskAwardRq.ext);
		getService(CountryService.class).getCountryGloryRq(this);
	}
}