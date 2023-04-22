package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class GetDefenceInfoHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetDefenceInfoRq req = msg.getExtension(WorldPb.GetDefenceInfoRq.ext);
        service.getDefenceInfoRq(req, this);
    }
}

