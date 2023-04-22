package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

public class ActOrderInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actOrderRq(this);
    }
}
