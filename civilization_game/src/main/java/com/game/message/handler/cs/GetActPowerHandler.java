package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.GetActPowerRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/10/22 16:05
 **/
public class GetActPowerHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).getActPowerRq(this, msg.getExtension(GetActPowerRq.ext));
	}
}
