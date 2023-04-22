package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.HeroService;

public class GetHeroHandler extends ClientHandler {

	@Override
	public void action() {
		// TODO Auto-generated method stub
		HeroService heroService = getService(HeroService.class);
		heroService.getHerosRq(this);
	}
}
