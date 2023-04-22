package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

// 参加国战
public class AttendCountryWarHandler extends ClientHandler {
	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.AttendCountryWarRq req = msg.getExtension(WorldPb.AttendCountryWarRq.ext);
		service.attendCountryWar(req, this);
	}
}
