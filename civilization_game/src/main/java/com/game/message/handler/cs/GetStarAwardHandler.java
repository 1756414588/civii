package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

public class GetStarAwardHandler extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        MissionPb.GetStarAwardRq req = msg.getExtension(MissionPb.GetStarAwardRq.ext);
        service.getStartInfoRq(req, this);
    }
}

