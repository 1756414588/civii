package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;

/**
 * 获取背包
 */
public class FlameBagHandler extends ClientHandler {

	@Override
	public void action() {
		getService(FlameWarService.class).bagHandler(this);
	}
}
