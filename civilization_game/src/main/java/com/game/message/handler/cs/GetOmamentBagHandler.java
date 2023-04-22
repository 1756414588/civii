package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.OmamentPb;
import com.game.service.OmamentService;

/**
*2020年8月5日
*@CaoBing
*halo_game
*GetOmamentBagHandler.java
*
*获取配饰背包协议
**/
public class GetOmamentBagHandler extends ClientHandler {

	@Override
	public void action() {
		OmamentService service = getService(OmamentService.class);
		OmamentPb.GetOmamentBagRq req = msg.getExtension(OmamentPb.GetOmamentBagRq.ext);
		service.getOmamentBagRq(req, this);
	}
}
