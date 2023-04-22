package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class RebuildCityHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.RebuildCityRq req = msg.getExtension(WorldPb.RebuildCityRq.ext);
        service.rebuildCityRq(req,this);
    }
}
