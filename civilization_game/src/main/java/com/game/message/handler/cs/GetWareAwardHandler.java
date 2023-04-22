package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.BuildingService;

public class GetWareAwardHandler  extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        service.getWareAward(this);
    }
}