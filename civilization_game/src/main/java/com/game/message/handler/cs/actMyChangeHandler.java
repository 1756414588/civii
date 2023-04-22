package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class actMyChangeHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityPb.DoHeroExchangelRq req = msg.getExtension(ActivityPb.DoHeroExchangelRq.ext);
        ActivityService service = getService(ActivityService.class);
        service.actMyChange(req, this);
    }
}
