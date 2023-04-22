package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

public class EquipPaperMissionHandler extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        MissionPb.EquipPaperMissionRq req = msg.getExtension(MissionPb.EquipPaperMissionRq.ext);
        service.equipPaperMissionRq(req, this);
    }
}
