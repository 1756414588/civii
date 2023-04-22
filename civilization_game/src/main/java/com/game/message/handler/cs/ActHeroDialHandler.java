package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class ActHeroDialHandler extends ClientHandler {


    @Override
    public void action() {

        ActivityService service = getService(ActivityService.class);
        ActivityPb.ActHeroDialRq req = msg.getExtension(ActivityPb.ActHeroDialRq.ext);
        service.actNewPurpDialRq(req ,this);

    }
}
