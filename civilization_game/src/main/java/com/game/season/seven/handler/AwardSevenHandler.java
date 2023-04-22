package com.game.season.seven.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class AwardSevenHandler extends ClientHandler {
	@Override
	public void action() {
		getService(SeasonService.class).awardSeven(msg.getExtension(SeasonActivityPb.AwardSevenRq.ext), this);
	}
}
