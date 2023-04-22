package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class LookHeroHandler extends ClientHandler {
    @Override
    public void action() {
        HeroService service = getService(HeroService.class);
        HeroPb.LookHeroRq req = msg.getExtension(HeroPb.LookHeroRq.ext);
        service.lookHero(req, this);
    }
}
