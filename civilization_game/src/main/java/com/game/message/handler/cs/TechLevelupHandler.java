package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.TechPb;
import com.game.service.TechService;

public class TechLevelupHandler  extends ClientHandler {
    @Override
    public void action () {
        TechService service = getService(TechService.class);
        TechPb.TechLevelupRq req = msg.getExtension(TechPb.TechLevelupRq.ext);
        service.techLevelup(req, this);
    }
}