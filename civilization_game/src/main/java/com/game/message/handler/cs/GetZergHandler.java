package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ZergPb.GetZergRq;
import com.game.service.ZergService;

public class GetZergHandler extends ClientHandler {

	@Override
	public void action() {
		ZergService service = getService(ZergService.class);
		service.getZerg( this);
	}
}
