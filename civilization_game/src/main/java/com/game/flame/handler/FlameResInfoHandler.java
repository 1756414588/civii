package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

/**
 * 查看 资源点
 */
public class FlameResInfoHandler extends ClientHandler {
	@Override
	public void action() {
		getService(FlameWarService.class).getResInfo(msg.getExtension(FlameWarPb.FlameResInfoRq.ext), this);
	}
}
