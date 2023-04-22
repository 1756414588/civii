package com.game.season.directgift.handler;

import com.game.message.handler.ClientHandler;
import com.game.season.SeasonService;

public class LoadGiftHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).loadGift(this);
	}
}
