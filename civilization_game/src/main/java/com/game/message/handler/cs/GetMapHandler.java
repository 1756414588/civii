package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class GetMapHandler extends ClientHandler {
    @Override
    public void action () {
        WorldService service = getService(WorldService.class);
        WorldPb.GetMapRq req = msg.getExtension(   WorldPb.GetMapRq.ext);
        service.getMap(req,this);
    }
}