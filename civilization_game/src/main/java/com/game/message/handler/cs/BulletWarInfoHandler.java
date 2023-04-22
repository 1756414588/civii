package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TDService;

public class BulletWarInfoHandler extends ClientHandler {
	@Override
	public void action() {
		getService(TDService.class).bulletWarInfo(this);
	}
}
