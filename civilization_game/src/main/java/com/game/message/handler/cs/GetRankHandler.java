package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RankPb.GetRankRq;
import com.game.service.RankService;

public class GetRankHandler extends ClientHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		// TODO Auto-generated method stub
		GetRankRq req = msg.getExtension(GetRankRq.ext);
		getService(RankService.class).getRankRq(req, this);
	}

}
