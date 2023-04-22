package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/11 16:37
 **/
public class ActSpringRechargeHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).actSpringRechargeRq(this);
	}
}
