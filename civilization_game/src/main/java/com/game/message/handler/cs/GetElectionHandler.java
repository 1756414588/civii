package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;


public class GetElectionHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetElectionRq req = msg.getExtension(WorldPb.GetElectionRq.ext);
        service.getElection(req,this);
    }
}
