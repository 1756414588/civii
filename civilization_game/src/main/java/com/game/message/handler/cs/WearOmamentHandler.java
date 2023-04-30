package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.service.OmamentService;

/**
*2020年8月7日
*
*halo_game
*WearOmamentHandler.java
**/
public class WearOmamentHandler extends ClientHandler {
	@Override
	public void action() {
		OmamentService service = getService(OmamentService.class);
		OmamentPb.WearOmamentRq req = msg.getExtension(OmamentPb.WearOmamentRq.ext);
		service.wearOmamentRq(req, this);
	}
}
