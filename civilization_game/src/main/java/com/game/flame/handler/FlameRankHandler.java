package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

public class FlameRankHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).getRank(msg.getExtension(FlameWarPb.FlameRankRq.ext), this);
	}
}
