package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldService;


public class GetWorldBossHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        service.getWorldBoss(this);
    }
}

