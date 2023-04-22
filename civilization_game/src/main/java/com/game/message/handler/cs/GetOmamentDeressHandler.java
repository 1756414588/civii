package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.service.OmamentService;

/**
*2020年8月5日
*@CaoBing
*halo_game
*GetOmamentDeressHandler.java
**/
public class GetOmamentDeressHandler extends ClientHandler {

	@Override
	public void action() {
		OmamentService service = getService(OmamentService.class);
		OmamentPb.GetOmamentDeressRq req = msg.getExtension(OmamentPb.GetOmamentDeressRq.ext);
		service.getOmamentDeressRq(req, this);
	}
}
