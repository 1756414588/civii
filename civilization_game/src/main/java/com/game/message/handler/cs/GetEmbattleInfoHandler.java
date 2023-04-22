package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class GetEmbattleInfoHandler extends ClientHandler {

    @Override
    public void action() {
        // TODO Auto-generated method stub
        HeroService heroService = getService(HeroService.class);
        msg.getExtension(HeroPb.GetEmbattleInfoRq.ext);
        heroService.getEmbattleInfoRq(this);
    }
}