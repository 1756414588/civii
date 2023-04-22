package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoHopeRq;
import com.game.service.ActivityService;

public class DoHopeHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).doHope(msg.getExtension(DoHopeRq.ext), this);
    }
}
