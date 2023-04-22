package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class DoRaidersHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).doRaiders(msg.getExtension(ActivityPb.DoRaidersRq.ext),this);
    }
}
