package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class LootOpenandler extends ClientHandler {
    @Override
    public void action() {
        HeroService heroService = getService(HeroService.class);
        HeroPb.LootOpenRq req = msg.getExtension(HeroPb.LootOpenRq.ext);
        heroService.lootOpen(req, this);
    }
}