package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;


public class BuyBuildQueHandler extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.BuyBuildQueCdRq req = msg.getExtension(BuildingPb.BuyBuildQueCdRq.ext);
        service.buyBuildQueCd(req, this);
    }
}