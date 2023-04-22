package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class ActLucklyAwardHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).doLucklyAward(msg.getExtension(ActivityPb.DoLucklyDialRq.ext),this);
    }
}
