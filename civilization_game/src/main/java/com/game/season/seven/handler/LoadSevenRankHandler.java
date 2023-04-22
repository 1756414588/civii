package com.game.season.seven.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class LoadSevenRankHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).loadSevenRank(msg.getExtension(SeasonActivityPb.LoadSevenRankRq.ext), this);
	}
}
