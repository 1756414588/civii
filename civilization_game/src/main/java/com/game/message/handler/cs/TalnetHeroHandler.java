package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class TalnetHeroHandler extends ClientHandler {
	@Override
	public void action() {

		getService(HeroService.class).telnetHero(msg.getExtension(HeroPb.TelnetHeroRq.ext), this);
	}
}
