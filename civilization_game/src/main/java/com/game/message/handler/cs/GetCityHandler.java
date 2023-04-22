package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class GetCityHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetCityRq req = msg.getExtension(WorldPb.GetCityRq.ext);
        service.getCityRq(req, this);
    }
}

