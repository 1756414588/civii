package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoWashEquiptRq;
import com.game.service.ActivityService;

public class DoWashEquiptHandler extends ClientHandler {

    @Override
    public void action() {
        getService(ActivityService.class)
            .doWashEquip(msg.getExtension(DoWashEquiptRq.ext), this);
    }
}