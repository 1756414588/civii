package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

public class FlameResRankHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).flameRankRes(msg.getExtension(FlameWarPb.FlameResRankRq.ext), this);
	}
}
