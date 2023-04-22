package com.game.season.talent.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class SelectSeasonTalentHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).selectSeasonTalent(msg.getExtension(SeasonActivityPb.SelectSeasonTalentRq.ext), this);
	}
}
