package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class CountryWarHandler extends ClientHandler {
	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.CountryWarRq req = msg.getExtension(WorldPb.CountryWarRq.ext);
		service.countryWarRq(req, this);
	}
}
