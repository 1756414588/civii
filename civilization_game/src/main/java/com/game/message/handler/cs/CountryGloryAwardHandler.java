package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class CountryGloryAwardHandler extends ClientHandler {
	@Override
	public void action() {
        CountryPb.CountryGloryAwardRq req = msg.getExtension(CountryPb.CountryGloryAwardRq.ext);
		getService(CountryService.class).countryGloryAwardRq(req,this);
	}
}