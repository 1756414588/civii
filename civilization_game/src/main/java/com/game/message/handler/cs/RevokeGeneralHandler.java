package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CountryPb;
import com.game.service.CountryService;

public class RevokeGeneralHandler extends ClientHandler {
	@Override
	public void action() {
		CountryPb.RevokeGeneralRq req = msg.getExtension(CountryPb.RevokeGeneralRq.ext);
		getService(CountryService.class).revokeGeneralRq(req, this);
	}
}