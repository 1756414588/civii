package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.PlayerService;

public class GetTimeHandler extends ClientHandler {
	@Override
	public void action() {
		PlayerService playerService = getService(PlayerService.class);
		playerService.getTime(this);
	}

}
