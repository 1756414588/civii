package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.CityService;

public class CityRemarkHandler extends ClientHandler {
	@Override
	public void action() {
		WorldPb.CityRemarkRq extension = msg.getExtension(WorldPb.CityRemarkRq.ext);
		getService(CityService.class).cityRemark(extension, this);
	}
}
