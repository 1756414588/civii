package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreApplyLineRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreApplyLineHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreApplyLineRq req = msg.getExtension(ActManoeuvreApplyLineRq.ext);
		service.actManoeuvreApplyLineRq(req, this);
	}
}
