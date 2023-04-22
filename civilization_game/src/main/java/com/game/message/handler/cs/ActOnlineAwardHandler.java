package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class ActOnlineAwardHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actOnlineAward(msg.getExtension(ActivityPb.ActOnlineAwardRq.ext),this);
    }
}
