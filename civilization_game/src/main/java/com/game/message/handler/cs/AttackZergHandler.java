package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ZergPb;
import com.game.service.ZergService;

public class AttackZergHandler extends ClientHandler {

	@Override
	public void action() {
		ZergService service = getService(ZergService.class);
		ZergPb.AttackZergRq req = msg.getExtension(ZergPb.AttackZergRq.ext);
		service.attendZergWar(req, this);
	}
}
