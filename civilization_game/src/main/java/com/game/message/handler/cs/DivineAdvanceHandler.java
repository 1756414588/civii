package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class DivineAdvanceHandler extends ClientHandler {
    @Override
    public void action() {
        HeroService heroService = getService(HeroService.class);
        HeroPb.DivineAdvanceRq req = msg.getExtension(HeroPb.DivineAdvanceRq.ext);
        heroService.divineAdvance(req, this);
    }
}