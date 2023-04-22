package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class ActRaidersInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).raidersInfo(msg.getExtension(ActivityPb.RaidersRq.ext),this);
    }
}
