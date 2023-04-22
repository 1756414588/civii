package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

/**
 * buff购买
 */
public class FlameWarBuyBuffHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).buyFlameWarBuff(this, msg.getExtension(FlameWarPb.FlameBuyBuffRq.ext));
	}
}
