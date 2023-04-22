package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class EmbattleHeroHandler extends ClientHandler {

    @Override
    public void action() {
        HeroService heroService = getService(HeroService.class);
        HeroPb.EmbattleHeroRq req = msg.getExtension(HeroPb.EmbattleHeroRq.ext);
        heroService.embattleHeroRq(req, this);
    }
}
