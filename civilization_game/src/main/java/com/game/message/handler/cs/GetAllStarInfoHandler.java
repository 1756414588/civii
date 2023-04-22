package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.MissionService;

public class GetAllStarInfoHandler  extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        service.getStartInfoRq(this);
    }
}

