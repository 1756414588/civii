package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.PlayerService;


public class AutoAddSoldierHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService service = getService(PlayerService.class);
        service.autoAddSoldier(this);
    }
}
