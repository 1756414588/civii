package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActManoeuvrePb.ActManoeuvreRecordRq;
import com.game.service.ActManoeuvreService;


public class ActManoeuvreRecordHandler extends ClientHandler {

	@Override
	public void action() {
		ActManoeuvreService service = getService(ActManoeuvreService.class);
		ActManoeuvreRecordRq req = msg.getExtension(ActManoeuvreRecordRq.ext);
		service.actManoeuvreRecordRq(req, this);
	}
}
