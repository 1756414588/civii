package com.game.season.journey.handler;

import com.game.message.handler.ClientHandler;
import com.game.season.SeasonService;

public class SeasonJourneyRankHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).getJourneyRank(this);
	}
}
