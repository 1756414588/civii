package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * 撤回行军
 */
public class MarchCancelHandler extends ClientHandler {

	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.MarchCancelRq req = msg.getExtension(WorldPb.MarchCancelRq.ext);
		service.marchCancel(req, this);
	}
}