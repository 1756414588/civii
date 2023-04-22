package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class ResBuildingDesHandler   extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.ResBuildingDesRq req = msg.getExtension(BuildingPb.ResBuildingDesRq.ext);
        service.destroyRes(req, this);
    }
}