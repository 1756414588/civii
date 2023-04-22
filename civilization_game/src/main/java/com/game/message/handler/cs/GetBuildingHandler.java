package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class GetBuildingHandler extends ClientHandler {
    @Override
    public void action() {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.GetBuildingRq req = msg.getExtension(BuildingPb.GetBuildingRq.ext);
        service.getBuildingRq(req, this);
    }
}