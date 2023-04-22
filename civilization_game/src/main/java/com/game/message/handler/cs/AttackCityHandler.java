package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * 攻打玩家基地
 */
public class AttackCityHandler extends ClientHandler {

	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.AttackCityRq req = msg.getExtension(WorldPb.AttackCityRq.ext);
		service.attackCityRq(req, this);
	}
}
