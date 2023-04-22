package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/27 17:22
 **/
public class BroodActHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).broodAct(this);
	}
}
