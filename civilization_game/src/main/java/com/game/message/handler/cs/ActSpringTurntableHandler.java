package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.ActSpringTurntableRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/11 16:37
 **/
public class ActSpringTurntableHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).actSpringTurntableRq(this, msg.getExtension(ActSpringTurntableRq.ext));
	}
}
