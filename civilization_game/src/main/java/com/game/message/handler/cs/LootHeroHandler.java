package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.HeroPb;
import com.game.service.HeroService;

public class LootHeroHandler extends ClientHandler {
    @Override
    public void action() {
        HeroService heroService = getService(HeroService.class);
        HeroPb.LootHeroRq req = msg.getExtension(HeroPb.LootHeroRq.ext);
        heroService.lootHero(req, this);
    }
}