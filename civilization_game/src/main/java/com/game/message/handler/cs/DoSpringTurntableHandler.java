package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoSpringTurntableRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/15 0:30
 **/
public class DoSpringTurntableHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).doSpringTurntableRq(this, msg.getExtension(DoSpringTurntableRq.ext));
	}
}
