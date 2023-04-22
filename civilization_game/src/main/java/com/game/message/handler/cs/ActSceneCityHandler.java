package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

public class ActSceneCityHandler extends ClientHandler {
	@Override
	public void action() {
		ActivityService service = getService(ActivityService.class);
		service.actSceneCityRq(this);
	}
}