package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class DoRecharDialHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).doRecharDialInfo(msg.getExtension(ActivityPb.DoRecharDialRq.ext),this);
    }
}
