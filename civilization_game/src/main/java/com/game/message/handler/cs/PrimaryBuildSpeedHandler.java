package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class PrimaryBuildSpeedHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.PrimaryBuildSpeedRq req = msg.getExtension(BuildingPb.PrimaryBuildSpeedRq.ext);
        service.primaryBuildSpeed(req, this);
    }
}