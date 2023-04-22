package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/15 10:38
 **/
public class ActSpringGiftHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).actSpringGiftRq(this);
	}
}
