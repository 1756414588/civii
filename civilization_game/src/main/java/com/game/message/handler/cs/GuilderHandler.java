package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class GuilderHandler extends ClientHandler {
	@Override
	public void action() {
		PlayerService playerService = getService(PlayerService.class);
		playerService.guidler(msg.getExtension(RolePb.GuilderRq.ext),this);
	}

}
