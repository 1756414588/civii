package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class SpeedMarchHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.SpeedMarchRq req = msg.getExtension(WorldPb.SpeedMarchRq.ext);
        service.speedMarch(req, this);
    }
}