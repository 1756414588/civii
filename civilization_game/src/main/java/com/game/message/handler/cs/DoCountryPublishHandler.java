package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class DoCountryPublishHandler extends ClientHandler {
	@Override
	public void action() {
		CountryPb.DoCountryPublishRq req = msg.getExtension(CountryPb.DoCountryPublishRq.ext);
		getService(CountryService.class).doCountryPublishRq(req, this);
	}
}