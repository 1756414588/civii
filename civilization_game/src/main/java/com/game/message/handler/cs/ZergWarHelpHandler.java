package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ZergPb.AttendZergCityRq;
import com.game.service.ZergService;

public class ZergWarHelpHandler extends ClientHandler {

	@Override
	public void action() {
		ZergService service = getService(ZergService.class);
		service.zergWarHelpRq(this);
	}
}
