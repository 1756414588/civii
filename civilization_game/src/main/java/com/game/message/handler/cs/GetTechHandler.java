package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TechService;


public class GetTechHandler extends ClientHandler {
    @Override
    public void action () {
        TechService service = getService(TechService.class);
        service.getTechInfo(this);
    }
}