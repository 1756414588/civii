package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;

/**
 * 活动面板
 */
public class FlameWarInitHandler extends ClientHandler {
	@Override
	public void action() {
		// FlameWarPb.FlameWarInitRq extension = msg.getExtension(FlameWarPb.FlameWarInitRq.ext);
		getService(FlameWarService.class).loadFlameWarInit(this);
	}
}
