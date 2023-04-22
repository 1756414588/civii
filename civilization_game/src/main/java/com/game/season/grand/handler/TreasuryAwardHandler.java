package com.game.season.grand.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class TreasuryAwardHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).treasuryAward(msg.getExtension(SeasonActivityPb.TreasuryAwardRq.ext), this);
	}
}
