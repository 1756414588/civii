package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;


public class HireDefendersHandler  extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.HireDefenderRq req = msg.getExtension(WallPb.HireDefenderRq.ext);
        service.hireDefenceRq(req, this);
    }
}

