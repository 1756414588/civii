package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class BuyBuildTeamHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.BuyBuildTeamRq req = msg.getExtension(BuildingPb.BuyBuildTeamRq.ext);
        service.buyBuildTeam(req, this);
    }
}