package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldService;

public class CallTransferHandler extends ClientHandler {
	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		service.callTransferRq(this);
	}
}
