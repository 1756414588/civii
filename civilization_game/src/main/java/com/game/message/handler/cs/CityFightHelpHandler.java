package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class CityFightHelpHandler extends ClientHandler {
	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.CityFightHelpRq req = msg.getExtension(WorldPb.CityFightHelpRq.ext);
		service.cityFightHelpRq(req, this);
	}
}
