package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class SearchResourceHandler extends ClientHandler {

	@Override
	public void action() {
		WorldPb.SearchEntityRq extension = msg.getExtension(WorldPb.SearchEntityRq.ext);
		getService(WorldService.class).searchResource(extension, this);
	}
}
