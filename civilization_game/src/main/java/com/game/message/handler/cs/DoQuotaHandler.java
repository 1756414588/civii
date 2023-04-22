package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoQuotaRq;
import com.game.service.ActivityService;

public class DoQuotaHandler extends ClientHandler {
	@Override
	public void action() {
		DoQuotaRq req = msg.getExtension(DoQuotaRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.doQuotaRq(req, this);
	}
}