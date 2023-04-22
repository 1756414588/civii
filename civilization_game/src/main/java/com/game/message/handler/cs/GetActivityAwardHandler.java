package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class GetActivityAwardHandler extends ClientHandler {
	@Override
	public void action() {
		ActivityPb.GetActivityAwardRq req = msg.getExtension(ActivityPb.GetActivityAwardRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.getActivityAward(req, this);
	}
}