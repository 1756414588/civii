package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class DoHeroDialHandler extends ClientHandler {


    @Override
    public void action() {

        ActivityPb.DoHeroDialRq req = msg.getExtension(ActivityPb.DoHeroDialRq.ext);
        ActivityService service = getService(ActivityService.class);
        service.doNewCommonPurp(req, this);

    }
}
