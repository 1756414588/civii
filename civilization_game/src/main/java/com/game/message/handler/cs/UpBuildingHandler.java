package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class UpBuildingHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.UpBuildingRq req = msg.getExtension(BuildingPb.UpBuildingRq.ext);
        service.upBuilding(req, this);
    }
}