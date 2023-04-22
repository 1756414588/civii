package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

public class FlameFightHelpHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).fightHelp(msg.getExtension(FlameWarPb.FlameFightHelpRq.ext), this);
	}
}
