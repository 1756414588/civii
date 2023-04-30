package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.service.OmamentService;

/**
*2020年8月7日
*
*halo_game
*CompoundOmamentHandler.java
**/
public class CompoundOmamentHandler extends ClientHandler {
	@Override
	public void action() {
		OmamentService service = getService(OmamentService.class);
		OmamentPb.CompoundOmamentRq req = msg.getExtension(OmamentPb.CompoundOmamentRq.ext);
		service.compoundOmamentRq(req, this);
	}
}
