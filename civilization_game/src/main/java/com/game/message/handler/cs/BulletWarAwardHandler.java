package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TDPb;
import com.game.service.TDService;

public class BulletWarAwardHandler extends ClientHandler {
	@Override
	public void action() {
		getService(TDService.class).bulletWarAward(msg.getExtension(TDPb.BulletWarLevelAwardRq.ext), this);
	}
}
