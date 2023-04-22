package com.game.season.hero;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.service.HeroService;

public class SeasonHeroHandler extends ClientHandler {
	@Override
	public void action() {
		getService(HeroService.class).upSkill(msg.getExtension(SeasonActivityPb.UpHeroLevelRq.ext), this);
	}
}
