package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.BuyLanternRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/15 0:28
 **/
public class BuyLanternHandler extends ClientHandler {

	@Override
	public void action() {
		getService(ActivityService.class).buyLanternRq(this, msg.getExtension(BuyLanternRq.ext));
	}
}
