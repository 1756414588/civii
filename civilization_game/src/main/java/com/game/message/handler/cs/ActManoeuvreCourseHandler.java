package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreCourseRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreCourseHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreCourseRq req = msg.getExtension(ActManoeuvreCourseRq.ext);
		service.actManoeuvreCourseRq(this);
	}
}
