package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

public class MissionDoneHandler extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        MissionPb.MissionDoneRq req = msg.getExtension(MissionPb.MissionDoneRq.ext);
        service.missionDoneRq(req, this);
    }
}
