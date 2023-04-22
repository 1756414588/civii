package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class GetWorldTargerAwardHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetWorldTargerAwardRq req = msg.getExtension(WorldPb.GetWorldTargerAwardRq.ext);
        service.getWorldTargerAward(req, this);
    }
}
