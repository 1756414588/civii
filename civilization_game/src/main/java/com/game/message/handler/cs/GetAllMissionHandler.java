package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

public class GetAllMissionHandler extends ClientHandler {
        @Override
        public void action() {
            MissionService service = getService(MissionService.class);
            MissionPb.GetAllMissionRq req = msg.getExtension(MissionPb.GetAllMissionRq.ext);
            service.getAllMissionRq(req, this);

        }
}