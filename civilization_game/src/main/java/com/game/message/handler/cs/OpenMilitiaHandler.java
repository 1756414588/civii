package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

// 开启名兵营
public class OpenMilitiaHandler  extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.OpenMilitiaRq req = msg.getExtension(BuildingPb.OpenMilitiaRq.ext);
        service.openMilitia(req, this);
    }
}