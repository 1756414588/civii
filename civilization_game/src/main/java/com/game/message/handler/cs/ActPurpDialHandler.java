package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.ActPurpDialRq;
import com.game.service.ActivityService;

public class ActPurpDialHandler extends ClientHandler {

    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActPurpDialRq req = msg.getExtension(ActPurpDialRq.ext);
        service.actPurpDialRq(req ,this);
    }
}
