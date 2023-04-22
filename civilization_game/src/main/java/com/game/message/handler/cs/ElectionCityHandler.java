package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

// 参加竞选
public class ElectionCityHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.ElectionCityRq req = msg.getExtension(WorldPb.ElectionCityRq.ext);
        service.electionCityRq(req,this);
    }
}

