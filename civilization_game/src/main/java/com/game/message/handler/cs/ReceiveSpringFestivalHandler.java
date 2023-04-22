package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.ReceiveSpringFestivalRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/15 0:29
 **/
public class ReceiveSpringFestivalHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).receiveSpringFestivalRq(this, msg.getExtension(ReceiveSpringFestivalRq.ext));
	}
}
