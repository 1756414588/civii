package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class AdvanceHeroHandler extends ClientHandler {
    @Override
    public void action() {
        HeroService heroService = getService(HeroService.class);
        HeroPb.AdvanceHeroRq req = msg.getExtension(HeroPb.AdvanceHeroRq.ext);
        heroService.advanceHero(req, this);
    }
}