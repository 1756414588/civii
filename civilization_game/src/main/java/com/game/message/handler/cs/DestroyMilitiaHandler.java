package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

public class DestroyMilitiaHandler  extends ClientHandler {
    @Override
    public void action () {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.RebuildMilitiaRq req = msg.getExtension(BuildingPb.RebuildMilitiaRq.ext);
        service.rebuildMilitia(req, this);
    }
}