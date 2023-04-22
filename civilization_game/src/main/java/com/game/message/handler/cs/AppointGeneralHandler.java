package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class AppointGeneralHandler extends ClientHandler {
	@Override
	public void action() {
		CountryPb.AppointGeneralRq req = msg.getExtension(CountryPb.AppointGeneralRq.ext);
		getService(CountryService.class).appointGeneralRq(req, this);
	}
}