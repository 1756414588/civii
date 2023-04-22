package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

public class FlameLoadMapHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).flameMap(msg.getExtension(FlameWarPb.FlameMapRq.ext), this);
	}
}
