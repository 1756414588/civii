package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class BuyHeroKowtowHandler extends ClientHandler {
	@Override
	public void action() {
		ActivityPb.BuyHeroKowtowRq req = msg.getExtension(ActivityPb.BuyHeroKowtowRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.buyHeroKowtowRq(req, this);
	}
}