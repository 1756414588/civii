package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class FixCityHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.FixCityRq req = msg.getExtension(WorldPb.FixCityRq.ext);
        service.fixCityRq(req,this);
    }
}
