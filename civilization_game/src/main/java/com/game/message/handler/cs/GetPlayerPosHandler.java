package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class GetPlayerPosHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetPlayerPosRq req = msg.getExtension(WorldPb.GetPlayerPosRq.ext);
        service.getPlayerPosRq(req,this);
    }
}
