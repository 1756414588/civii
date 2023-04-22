package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class GetCityOwnHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetCityOwnRq req = msg.getExtension(WorldPb.GetCityOwnRq.ext);
        service.getCityOwnRq(req,this);
    }
}
