package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoLuckDialRq;
import com.game.service.ActivityService;

public class DoLuckDialHandler extends ClientHandler {
	@Override
	public void action() {
		DoLuckDialRq req = msg.getExtension(DoLuckDialRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.doLuckDialRq(req, this);
	}
}