package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.service.OmamentService;

/**
*2020年8月7日
*
*halo_game
*TakeOffOmamentHandler.java
**/
public class TakeOffOmamentHandler extends ClientHandler {
	@Override
	public void action() {
		OmamentService service = getService(OmamentService.class);
		OmamentPb.TakeOffOmamentRq req = msg.getExtension(OmamentPb.TakeOffOmamentRq.ext);
		service.takeOffOmamentRq(req, this);
	}
}
