package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.service.BuildingService;

public class DoRescoureHandler extends ClientHandler {
    @Override
    public void action() {
        BuildingService service = getService(BuildingService.class);
        service.doRescoureRq(this);
    }
}