package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RankPb.GetAreaRankRq;
import com.game.service.RankService;

public class GetAreaRankHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		GetAreaRankRq req = msg.getExtension(GetAreaRankRq.ext);
		getService(RankService.class).getAreaRankRq(req, this);
	}

}
