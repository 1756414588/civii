package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class BuildResHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.BuildResRq req = msg.getExtension(BuildingPb.BuildResRq.ext);
        service.buildRes(req, this);
    }
}