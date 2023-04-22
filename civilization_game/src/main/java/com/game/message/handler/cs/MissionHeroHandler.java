package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

public class MissionHeroHandler extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        MissionPb.HeroMissionRq req = msg.getExtension(MissionPb.HeroMissionRq.ext);
        if (req.hasHeroId()) {
            service.heroHired(req, this);
        } else {
            service.heroHiredNew(req, this);
        }
    }
}
