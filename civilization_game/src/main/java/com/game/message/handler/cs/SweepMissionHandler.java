package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

public class SweepMissionHandler extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        MissionPb.SweepMissionRq req = msg.getExtension(MissionPb.SweepMissionRq.ext);
        service.sweepMission(req, this);
    }
}

