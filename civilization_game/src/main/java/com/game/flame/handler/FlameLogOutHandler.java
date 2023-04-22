package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.DealType;
import com.game.server.GameServer;

/**
 * 推出活动
 */
public class FlameLogOutHandler extends ClientHandler {
	@Override
	public void action() {
		GameServer.getInstance().mainLogicServer.addCommand(() -> {
			getService(FlameWarService.class).logOutMap(this);
		}, DealType.MAIN);
	}
}
