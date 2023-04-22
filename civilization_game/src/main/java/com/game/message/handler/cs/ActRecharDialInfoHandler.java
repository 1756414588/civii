package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

public class ActRecharDialInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).recharDialInfo(this);
    }
}
