package com.game.season;

import com.game.message.handler.ClientHandler;

public class LoadSeasonActHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).loadSeasonAct(this);
	}
}
