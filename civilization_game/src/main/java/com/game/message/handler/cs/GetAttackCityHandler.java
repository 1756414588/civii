package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class GetAttackCityHandler extends ClientHandler {
    @Override
    public void action () {
        WorldService service = getService(WorldService.class);
        WorldPb.GetPvpCityRq req = msg.getExtension(WorldPb.GetPvpCityRq.ext);
        service.getAttackCity(req, this);
    }
}
