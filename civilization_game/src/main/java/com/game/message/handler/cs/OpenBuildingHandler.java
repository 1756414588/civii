package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class OpenBuildingHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.OpenBuildingRq req = msg.getExtension(BuildingPb.OpenBuildingRq.ext);
        service.openBuilding(req, this);
    }
}