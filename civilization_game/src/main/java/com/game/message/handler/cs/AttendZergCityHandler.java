package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ZergPb.AttendZergCityRq;
import com.game.service.ZergService;

public class AttendZergCityHandler extends ClientHandler {

	@Override
	public void action() {
		ZergService service = getService(ZergService.class);
		AttendZergCityRq req = msg.getExtension(AttendZergCityRq.ext);
		service.attendZergCityRq(req, this);
	}
}
