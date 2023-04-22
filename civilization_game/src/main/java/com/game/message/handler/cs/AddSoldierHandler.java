package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class AddSoldierHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.AddSoldierRq req = msg.getExtension(WorldPb.AddSoldierRq.ext);
        service.addSoldierRq(req, this);
    }
}