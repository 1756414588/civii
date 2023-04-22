package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class CountryTaskAwardHandler extends ClientHandler {
	@Override
	public void action() {
		CountryPb.CountryTaskAwardRq req = msg.getExtension(CountryPb.CountryTaskAwardRq.ext);
		getService(CountryService.class).countryTaskAwardRq(req, this);
	}
}