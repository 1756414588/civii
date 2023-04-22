package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class DevCityHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.DevCityRq req = msg.getExtension(WorldPb.DevCityRq.ext);
        service.devCityRq(req, this);
    }
}
