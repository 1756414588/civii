package com.game.season.turn.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class TurnAwardHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).turnAward(msg.getExtension(SeasonActivityPb.DoSeasonAwardRq.ext), this);
	}
}
