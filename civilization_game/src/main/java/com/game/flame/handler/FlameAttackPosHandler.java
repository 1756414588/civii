package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.DealType;
import com.game.pb.FlameWarPb;
import com.game.server.GameServer;

/**
 * 攻打，占领
 */
public class FlameAttackPosHandler extends ClientHandler {
	@Override
	public void action() {
		 GameServer.getInstance().mainLogicServer.addCommand(() -> {
			getService(FlameWarService.class).attackFlame(msg.getExtension(FlameWarPb.AttackFlameRq.ext), this);
		}, DealType.MAIN);
	}
}
