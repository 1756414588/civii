package com.game.season.talent.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class UpSeasonTalentHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).upSeasonTalent(msg.getExtension(SeasonActivityPb.UpSeasonTalentRq.ext), this);
	}
}
