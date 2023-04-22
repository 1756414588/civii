package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb.ResourceMissionRq;
import com.game.service.MissionService;

public class ResourceMissionHandler extends ClientHandler {
    @Override
    public void action() {
        MissionService service = getService(MissionService.class);
        ResourceMissionRq req = msg.getExtension(ResourceMissionRq.ext);
        service.resourceRq(req, this);
    }
}
