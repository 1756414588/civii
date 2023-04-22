package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.TechPb;
import com.game.service.TechService;

public class TechKillCdHandler extends ClientHandler {
    @Override
    public void action () {
        TechService service = getService(TechService.class);
        TechPb.TechKillCdRq req = msg.getExtension(TechPb.TechKillCdRq.ext);
        service.techKillCdRq(req, this);
    }
}