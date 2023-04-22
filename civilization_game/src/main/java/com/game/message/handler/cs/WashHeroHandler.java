package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class WashHeroHandler  extends ClientHandler {
    @Override
    public void action() {
        HeroService heroService = getService(HeroService.class);
        HeroPb.WashHeroRq req = msg.getExtension(HeroPb.WashHeroRq.ext);
        heroService.washHeroRq(req, this);
    }
}