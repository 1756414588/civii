package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class GetMarchHandler  extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetMarchRq req = msg.getExtension(WorldPb.GetMarchRq.ext);
        service.getMarchRq(req, this);
    }
}