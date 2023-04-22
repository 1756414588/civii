package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class CancelCityOwnerHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.CancelCityOwnerRq req = msg.getExtension(WorldPb.CancelCityOwnerRq.ext);
        service.cancelCityOwner(req,this);
    }
}

