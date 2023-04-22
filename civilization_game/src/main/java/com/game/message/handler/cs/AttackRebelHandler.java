package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * 攻打地图野怪
 */
public class AttackRebelHandler extends ClientHandler {

	@Override
	public void action() {
		WorldService service = getService(WorldService.class);
		WorldPb.AttackRebelRq req = msg.getExtension(WorldPb.AttackRebelRq.ext);
		service.attackRebelRq(req, this);
	}
}