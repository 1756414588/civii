package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

/**
 * 领取产出物质
 */
public class FlameReceiveHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).receiveHandler(msg.getExtension(FlameWarPb.ReceiveBuildAwardRq.ext), this);
	}
}
