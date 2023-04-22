package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoSevenAwardRq;
import com.game.service.ActivityService;

public class DoSevenAwardHandler extends ClientHandler {
	@Override
	public void action() {
		DoSevenAwardRq req = msg.getExtension(DoSevenAwardRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.doSevenAwardRq(req, this);
	}
}