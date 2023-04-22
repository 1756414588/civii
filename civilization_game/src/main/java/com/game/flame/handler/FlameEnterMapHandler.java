package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.DealType;
import com.game.server.GameServer;

/**
 * 进入地图
 */
public class FlameEnterMapHandler extends ClientHandler {
	@Override
	public void action() {
		GameServer.getInstance().mainLogicServer.addCommand(() -> {
			getService(FlameWarService.class).enterFlameMap(this);
		}, DealType.MAIN);
	}
}
