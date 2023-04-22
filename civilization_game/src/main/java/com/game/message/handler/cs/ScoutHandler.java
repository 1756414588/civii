package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class ScoutHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.ScoutRq req = msg.getExtension(WorldPb.ScoutRq.ext);
        service.scoutRq(req, this);
    }
}
