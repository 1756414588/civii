package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MapInfoPb.GetMapNpcRq;
import com.game.service.MapInfoService;

public class GetMapNpcHandler extends ClientHandler {

	@Override
	public void action() {
		MapInfoService service = getService(MapInfoService.class);
		GetMapNpcRq req = msg.getExtension(GetMapNpcRq.ext);
		service.getMapNpcRq(req, this);
	}
}