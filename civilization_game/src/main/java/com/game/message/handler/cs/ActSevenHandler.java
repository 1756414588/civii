package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.ActSevenRq;
import com.game.service.ActivityService;

public class ActSevenHandler extends ClientHandler {
	@Override
	public void action() {
		ActSevenRq req = msg.getExtension(ActSevenRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.actSevenRq(req, this);
	}
}